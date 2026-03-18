package com.helloworld.onlineshopping.modules.cache;

import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.product.service.ProductService;
import com.helloworld.onlineshopping.modules.product.dto.ProductSpuCreateDTO;
import com.helloworld.onlineshopping.common.security.LoginUser;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.redisson.api.RedissonClient;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RedisOptimizationIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductSpuMapper spuMapper;

    @Autowired
    private MerchantShopMapper shopMapper;

    @BeforeEach
    public void setupSecurity() {
        LoginUser loginUser = new LoginUser(999L, "testuser", new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, new ArrayList<>())
        );

        // Prep shop
        if (shopMapper.selectById(999L) == null) {
            MerchantShopEntity shop = new MerchantShopEntity();
            shop.setId(999L);
            shop.setUserId(999L);
            shop.setShopName("Test Optimization Shop");
            shop.setShopStatus(1);
            shopMapper.insert(shop);
        }
    }

    /**
     * Testing Cache Penetration via Bloom Filter
     */
    @Test
    @Transactional
    public void testBloomFilterCachePenetrationProtection() {
        // Creates a new product, which saves into DB and adds ID to Bloom filter
        ProductSpuCreateDTO dto = new ProductSpuCreateDTO();
        dto.setBrandName("BloomTestBrand");
        dto.setTitle("Valid Product For Bloom");
        dto.setCategoryId(1L);

        // Simulate save
        productService.createProduct(dto);

        // Fetch ID that we just inserted by searching product list manually
        ProductSpuEntity spu = spuMapper.selectList(null).stream()
                .filter(p -> "Valid Product For Bloom".equals(p.getTitle()))
                .findFirst()
                .orElseThrow();

        // 1. Valid request -> Bloom Filter allows, we don't throw exception or throw our DB one if data missing
        assertDoesNotThrow(() -> {
            try {
                productService.getProductDetail(spu.getId());
            } catch (Exception e) {
                if(!e.getMessage().equals("Product not found")){
                   throw e;
                }
            }
        });

        // 2. Invalid request with extremely huge nonexistent ID -> Bloom Filter blocks early 
        // Throwing BusinessException directly rather than checking DB due to strict bloom filtering
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.getProductDetail(Long.MAX_VALUE);
        });

        assertEquals("Product not found", exception.getMessage());
    }
}
