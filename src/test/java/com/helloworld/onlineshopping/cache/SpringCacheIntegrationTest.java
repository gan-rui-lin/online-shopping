package com.helloworld.onlineshopping.cache;

import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.product.service.ProductService;
import com.helloworld.onlineshopping.modules.product.vo.ProductDetailVO;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@SpringBootTest
public class SpringCacheIntegrationTest {

    @Autowired
    private ProductService productService;

    @SpyBean
    private ProductSpuMapper productSpuMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MerchantShopMapper merchantShopMapper;

    @Autowired
    private org.redisson.api.RedissonClient redissonClient;

    @Test
    @DisplayName("测试商品详情的读缓存与驱逐(Cacheable & CacheEvict)是否生效")
    @Transactional
    public void testProductDetailCache() {
        // 先插入伪造的店铺，避免后续跑 updateProductStatus 时抛出“无权限修改”异常
        MerchantShopEntity testShop = new MerchantShopEntity();
        testShop.setUserId(1L); // 与后续强制指定的 LoginUser ID 对应
        testShop.setShopName("测试店铺");
        testShop.setShopStatus(1);
        merchantShopMapper.insert(testShop);

        // 1. 在数据库中真实造一条测试数据，模拟已存在的商品
        ProductSpuEntity testProduct = new ProductSpuEntity();
        testProduct.setBrandName("Cache验证测试品牌");
        testProduct.setTitle("缓存测试专用商品");
        testProduct.setSubTitle("用于整合测试的商品");
        testProduct.setCategoryId(1L);
        testProduct.setShopId(testShop.getId());
        testProduct.setStatus(1); // 默认状态
        testProduct.setAuditStatus(1);
        
        // 真实插入数据库，MyBatis-Plus会回填ID
        productSpuMapper.insert(testProduct);
        Long testSpuId = testProduct.getId();
        
        // 由于是手动在Mapper层面插入数据，绕过了 ProductService.createProduct()
        // 因此需要手动将测试商品的 ID 添加到布隆过滤器，防止 getProductDetail 被拦截
        org.redisson.api.RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("product:bloom:filter");
        bloomFilter.add(testSpuId);
        
        String cacheKey = "product:detail::" + testSpuId;

        // 2. 测试开始前清理缓存（保险起见）
        redisTemplate.delete(cacheKey);

        // 3. 第一次查询（触发 Cacheable，缓存未命中，将查询数据库并写入 Redis）
        ProductDetailVO firstCall = productService.getProductDetail(testSpuId);
        Assertions.assertNotNull(firstCall);
        Assertions.assertEquals("缓存测试专用商品", firstCall.getTitle());

        // 4. 断言第一次调用后，Redis 中确实生成对应的 Cache Key
        Boolean hasKey = redisTemplate.hasKey(cacheKey);
        Assertions.assertTrue(hasKey, "Redis 中应该存在缓存 " + cacheKey);

        // 5. 验证过期时间 (TTL) 是否设置成了约 30 分钟 (包含抖动时间：可能在 30 ~ 36 分钟之间)
        Long expire = redisTemplate.getExpire(cacheKey, TimeUnit.MINUTES);
        Assertions.assertNotNull(expire);
        Assertions.assertTrue(expire > 28 && expire <= 36, "TTL 应该在 30 分钟左右(由于有随机抖动防雪崩，范围大概在30~36)，实际是: " + expire);

        // 6. 重置 Mapper 的调用监控计数器
        reset(productSpuMapper);

        // 7. 第二次查询，应当直接从 Redis 缓存中获取
        ProductDetailVO secondCall = productService.getProductDetail(testSpuId);
        Assertions.assertNotNull(secondCall);
        
        // ⭐拦截验证：确保第二次没有再去执行 selectById 查询数据库
        verify(productSpuMapper, never()).selectById(anyLong());
        System.out.println("✅ Spring Cache (@Cacheable) 读缓存验证成功！");

        // === 伪造 Spring Security 登录上下文 ===
        // 因为 updateProductStatus 等方法会调用 SecurityUtil 获取当前登录用户，不伪造会报 Not authenticated
        com.helloworld.onlineshopping.common.security.LoginUser mockLoginUser = 
                new com.helloworld.onlineshopping.common.security.LoginUser(1L, "mock_admin", java.util.Collections.emptyList());
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockLoginUser, null, java.util.Collections.emptyList());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        // 8. 验证 CacheEvict 缓存驱逐功能
        // 调用更新接口（如更新商品状态），这个方法上应该打了 @CacheEvict 注解
        productService.updateProductStatus(testSpuId, 0);

        // 9. 更新逻辑执行完毕后，Redis 里的缓存应被删除
        Boolean keyExistsAfterUpdate = redisTemplate.hasKey(cacheKey);
        Assertions.assertFalse(keyExistsAfterUpdate, "执行更新操作后，对应的缓存数据必须被清理删除");
        System.out.println("✅ Spring Cache (@CacheEvict) 缓存驱逐验证成功！");
    }
}
