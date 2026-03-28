package com.helloworld.onlineshopping.modules.file;

import com.helloworld.onlineshopping.modules.file.service.FileRecordService;
import com.helloworld.onlineshopping.modules.file.service.FileStorageService;
import com.helloworld.onlineshopping.common.utils.JwtUtil;
import com.helloworld.onlineshopping.utils.ImageProcessor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private FileRecordService fileRecordService;

    // JwtAuthenticationFilter needs JwtUtil as a bean; provide a mock so the WebMvcTest slice can start
    @MockBean
    private JwtUtil jwtUtil;

    private MockedStatic<ImageProcessor> imageProcessorStatic;

    @BeforeEach
    public void setup() {
        given(fileStorageService.uploadFile(any(byte[].class), anyString())).willReturn("https://example.com/file.png");
        given(fileStorageService.generateSignedUrl(anyString())).willReturn("https://example.com/signed/file.png");

        // Mock static ImageProcessor methods to avoid real image processing in controller tests
        imageProcessorStatic = Mockito.mockStatic(ImageProcessor.class);
        imageProcessorStatic.when(() -> ImageProcessor.generateThumbnail(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt()))
                .thenAnswer(invocation -> (byte[]) invocation.getArgument(0));
        imageProcessorStatic.when(() -> ImageProcessor.addWatermark(Mockito.any(byte[].class), Mockito.anyString(), Mockito.anyString(), Mockito.anyFloat(), Mockito.anyInt()))
                .thenAnswer(invocation -> (byte[]) invocation.getArgument(0));
        imageProcessorStatic.when(() -> ImageProcessor.normalizeMainImage(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyFloat()))
                .thenAnswer(invocation -> (byte[]) invocation.getArgument(0));
        imageProcessorStatic.when(() -> ImageProcessor.compressImage(Mockito.any(byte[].class), Mockito.anyFloat()))
                .thenAnswer(invocation -> (byte[]) invocation.getArgument(0));
    }

    @AfterEach
    public void tearDown() {
        if (imageProcessorStatic != null) {
            imageProcessorStatic.close();
        }
    }

    @Test
    public void testUploadFile() throws Exception {
        byte[] img = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", img);

        mockMvc.perform(multipart("/api/files/upload").file(file).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").exists())
                .andExpect(jsonPath("$.thumbnailUrl").value("https://example.com/file.png"));
    }

    @Test
    public void testUploadMultipleFiles() throws Exception {
        byte[] img = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        MockMultipartFile file1 = new MockMultipartFile("files", "a.png", "image/png", img);
        MockMultipartFile file2 = new MockMultipartFile("files", "b.png", "image/png", img);

        mockMvc.perform(multipart("/api/files/upload/multiple").file(file1).file(file2).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files[0].fileName").exists())
                .andExpect(jsonPath("$.files[0].thumbnailUrl").value("https://example.com/file.png"));
    }

    @Test
    public void testUploadLocalFolder() throws Exception {
        byte[] img = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        Path folder = Files.createTempDirectory("local-image-upload-test");
        Files.write(folder.resolve("sample.png"), img);

        mockMvc.perform(multipart("/api/files/upload/local-folder")
                        .param("folderPath", folder.toString())
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.files[0].fileName").exists())
                .andExpect(jsonPath("$.files[0].url").value("https://example.com/file.png"));
    }
}
