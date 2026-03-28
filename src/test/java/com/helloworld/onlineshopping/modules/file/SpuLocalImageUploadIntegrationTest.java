package com.helloworld.onlineshopping.modules.file;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helloworld.onlineshopping.common.security.LoginUser;
import com.helloworld.onlineshopping.modules.product.dto.ProductImageBindDTO;
import com.helloworld.onlineshopping.modules.product.dto.ProductImageBindItemDTO;
import com.helloworld.onlineshopping.modules.product.entity.ProductImageEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductImageMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SpuLocalImageUploadIntegrationTest {

    private static final Long OVERRIDE_TEST_MERCHANT_USER_ID =
            Long.parseLong(System.getProperty("test.merchant.userId", "0"));
    private static final String DEFAULT_TEST_MERCHANT_USERNAME =
            System.getProperty("test.merchant.username", "merchant_test");
    private static final Long DEFAULT_TEST_SPU_ID =
            Long.parseLong(System.getProperty("test.spu.id", "1010"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductSpuMapper productSpuMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${file.upload.base-dir}")
    private String uploadBaseDir;

    private Path tempSourceImagePath;
    private Path tempUploadFolder;
    private Path uploadedFilePath;

    @AfterEach
    void cleanup() throws IOException {
        SecurityContextHolder.clearContext();
        if (shouldCleanupUploadedFile() && uploadedFilePath != null && Files.exists(uploadedFilePath)) {
            Files.delete(uploadedFilePath);
        }
        if (tempSourceImagePath != null && Files.exists(tempSourceImagePath)) {
            Files.delete(tempSourceImagePath);
        }
        if (tempUploadFolder != null && Files.exists(tempUploadFolder)) {
            try (var stream = Files.walk(tempUploadFolder)) {
                stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    @Test
    void shouldUploadLocalJpgAndBindSpuMainImageByApiFlow() throws Exception {
        ensureFileRecordTableExists();

        var merchantAuth = buildMerchantAuthentication(DEFAULT_TEST_SPU_ID);

        Path localImagePath = resolveSourceImage();
        tempUploadFolder = Files.createTempDirectory("spu-upload-api-folder-");
        Path copiedImagePath = tempUploadFolder.resolve(localImagePath.getFileName().toString());
        Files.copy(localImagePath, copiedImagePath, StandardCopyOption.REPLACE_EXISTING);

        MvcResult uploadResult = mockMvc.perform(post("/api/files/upload/local-folder")
                        .with(authentication(merchantAuth))
                        .param("folderPath", tempUploadFolder.toString())
                        .param("category", "spu-" + DEFAULT_TEST_SPU_ID)
                        .param("recursive", "false")
                        .param("generateThumbnail", "false")
                        .param("addWatermark", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andReturn();

        JsonNode uploadJson = parseBody(uploadResult);
        JsonNode firstFile = uploadJson.path("files").get(0);
        String uploadedUrl = firstFile.path("url").asText();
        assertTrue(!firstFile.path("category").asText("").startsWith("Failed:"),
                "Upload should succeed but got: " + firstFile.path("category").asText());
        assertTrue(StringUtils.hasText(uploadedUrl), "Uploaded url should not be empty");

        String uploadedFileName = extractFileName(uploadedUrl);
        uploadedFilePath = Paths.get(uploadBaseDir).toAbsolutePath().normalize().resolve(uploadedFileName);
        assertTrue(Files.exists(uploadedFilePath), "Uploaded file should exist in upload directory");
        BufferedImage normalizedImage = ImageIO.read(uploadedFilePath.toFile());
        assertNotNull(normalizedImage, "Uploaded main image should be readable");
        assertEquals(800, normalizedImage.getWidth(), "SPU main image width should be normalized to 800");
        assertEquals(800, normalizedImage.getHeight(), "SPU main image height should be normalized to 800");

        ProductImageBindItemDTO spuMain = new ProductImageBindItemDTO();
        spuMain.setImageType(1);
        spuMain.setImageUrl(uploadedUrl);
        spuMain.setSortOrder(0);

        ProductImageBindDTO bindDTO = new ProductImageBindDTO();
        bindDTO.setMainImageUrl(uploadedUrl);
        bindDTO.setImages(List.of(spuMain));

        String bindPayload = objectMapper.writeValueAsString(bindDTO);
        mockMvc.perform(put("/api/products/{spuId}/images", DEFAULT_TEST_SPU_ID)
                        .with(authentication(merchantAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bindPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ProductSpuEntity updatedSpu = productSpuMapper.selectById(DEFAULT_TEST_SPU_ID);
        assertNotNull(updatedSpu, "SPU should exist");
        assertEquals(uploadedUrl, updatedSpu.getMainImage(), "SPU main_image should be updated");

        ProductImageEntity mainImageRecord = productImageMapper.selectOne(
                new LambdaQueryWrapper<ProductImageEntity>()
                        .eq(ProductImageEntity::getSpuId, DEFAULT_TEST_SPU_ID)
                        .eq(ProductImageEntity::getImageType, 1)
                        .eq(ProductImageEntity::getImageUrl, uploadedUrl)
                        .last("LIMIT 1")
        );
        assertNotNull(mainImageRecord, "product_image record should be inserted for SPU main image");

        finishTransactionBySwitch();
    }

    private void finishTransactionBySwitch() {
        if (!TestTransaction.isActive()) {
            return;
        }
        if (isRollbackEnabled()) {
            TestTransaction.flagForRollback();
        } else {
            TestTransaction.flagForCommit();
        }
        TestTransaction.end();
    }

    private boolean isRollbackEnabled() {
        return Boolean.parseBoolean(System.getProperty("test.spu.image.rollback", "true"));
    }

    private boolean shouldCleanupUploadedFile() {
        String value = System.getProperty("test.spu.image.cleanupFile");
        if (StringUtils.hasText(value)) {
            return Boolean.parseBoolean(value);
        }
        return isRollbackEnabled();
    }

    private void ensureFileRecordTableExists() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS file_record (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    file_name VARCHAR(255) NOT NULL,
                    original_name VARCHAR(255) NOT NULL,
                    file_type VARCHAR(64),
                    file_size BIGINT NOT NULL,
                    url VARCHAR(500) NOT NULL,
                    thumbnail_url VARCHAR(500),
                    category VARCHAR(64),
                    uploader_id VARCHAR(64),
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    deleted TINYINT NOT NULL DEFAULT 0
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
    }

    private JsonNode parseBody(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(content);
    }

    private UsernamePasswordAuthenticationToken buildMerchantAuthentication(Long spuId) {
        Long merchantUserId = resolveMerchantUserIdBySpuId(spuId);
        LoginUser loginUser = new LoginUser(
                merchantUserId,
                DEFAULT_TEST_MERCHANT_USERNAME,
                List.of("ROLE_MERCHANT")
        );
        return new UsernamePasswordAuthenticationToken(
                loginUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))
        );
    }

    private Long resolveMerchantUserIdBySpuId(Long spuId) {
        if (OVERRIDE_TEST_MERCHANT_USER_ID > 0) {
            return OVERRIDE_TEST_MERCHANT_USER_ID;
        }
        Long userId = jdbcTemplate.queryForObject(
                """
                SELECT ms.user_id
                FROM product_spu ps
                JOIN merchant_shop ms ON ps.shop_id = ms.id
                WHERE ps.id = ? AND ps.deleted = 0 AND ms.deleted = 0
                """,
                Long.class,
                spuId
        );
        if (userId == null) {
            throw new IllegalStateException("No merchant owner found for spuId=" + spuId);
        }
        return userId;
    }

    private String extractFileName(String url) {
        int idx = url.lastIndexOf('/');
        return idx >= 0 ? url.substring(idx + 1) : url;
    }

    private Path resolveSourceImage() throws IOException {
        String configuredPath = System.getProperty("test.local.image.path");
        if (StringUtils.hasText(configuredPath)) {
            Path localPath = Paths.get(configuredPath).toAbsolutePath().normalize();
            if (!Files.exists(localPath)) {
                throw new IllegalArgumentException("Configured local image does not exist: " + localPath);
            }
            return localPath;
        }

        // Fallback image keeps this integration test self-contained when no local file path is provided.
        Path tempImage = Files.createTempFile("spu-upload-source-", ".jpg");
        tempSourceImagePath = tempImage;
        Files.write(tempImage, createSampleJpeg());
        return tempImage;
    }

    private byte[] createSampleJpeg() throws IOException {
        BufferedImage image = new BufferedImage(320, 320, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setColor(new Color(20, 120, 220));
            g2d.fillRect(0, 0, 320, 320);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 28));
            g2d.drawString("iPhone 15", 70, 150);
            g2d.drawString("Pro Max", 78, 195);
        } finally {
            g2d.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return output.toByteArray();
    }
}


