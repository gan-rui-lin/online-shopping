package com.helloworld.onlineshopping.modules.product;

import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.LoginUser;
import com.helloworld.onlineshopping.modules.behavior.service.BrowseHistoryService;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.product.dto.ProductImageBindDTO;
import com.helloworld.onlineshopping.modules.product.dto.ProductImageBindItemDTO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductImageEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductImageMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.product.search.EsProductService;
import com.helloworld.onlineshopping.modules.product.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImageBindTest {

    @Mock
    private ProductSpuMapper spuMapper;
    @Mock
    private ProductSkuMapper skuMapper;
    @Mock
    private ProductImageMapper imageMapper;
    @Mock
    private MerchantShopMapper shopMapper;
    @Mock
    private BrowseHistoryService browseHistoryService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private EsProductService esProductService;
    @Mock
    private ObjectProvider<EsProductService> esProductServiceProvider;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setup() {
        LoginUser loginUser = new LoginUser(9L, "merchant", List.of("ROLE_MERCHANT"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, null));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bindProductImages_shouldSyncSpuAndSkuImages_whenRequestValid() {
        when(esProductServiceProvider.getIfAvailable()).thenReturn(esProductService);

        Long spuId = 1002L;
        ProductSpuEntity spu = new ProductSpuEntity();
        spu.setId(spuId);
        spu.setShopId(100L);
        when(spuMapper.selectById(spuId)).thenReturn(spu);

        MerchantShopEntity shop = new MerchantShopEntity();
        shop.setId(100L);
        when(shopMapper.selectOne(any())).thenReturn(shop);

        ProductSkuEntity sku = new ProductSkuEntity();
        sku.setId(5001L);
        sku.setSpuId(spuId);
        when(skuMapper.selectById(5001L)).thenReturn(sku);

        ProductImageBindItemDTO spuMain = new ProductImageBindItemDTO();
        spuMain.setImageType(1);
        spuMain.setImageUrl("'/cdn/images/AirPods Pro 2.jpg'");
        spuMain.setSortOrder(0);

        ProductImageBindItemDTO detail = new ProductImageBindItemDTO();
        detail.setImageType(2);
        detail.setImageUrl("/cdn/images/AirPods Pro 2-detail.jpg");
        detail.setSortOrder(1);

        ProductImageBindItemDTO skuImage = new ProductImageBindItemDTO();
        skuImage.setImageType(3);
        skuImage.setSkuId(5001L);
        skuImage.setImageUrl("/cdn/images/AirPods Pro 2-sku.jpg");
        skuImage.setSortOrder(0);

        ProductImageBindDTO dto = new ProductImageBindDTO();
        dto.setImages(List.of(spuMain, detail, skuImage));

        productService.bindProductImages(spuId, dto);

        ArgumentCaptor<ProductSpuEntity> spuCaptor = ArgumentCaptor.forClass(ProductSpuEntity.class);
        verify(spuMapper).updateById(spuCaptor.capture());
        assertEquals("/cdn/images/AirPods Pro 2.jpg", spuCaptor.getValue().getMainImage());

        ArgumentCaptor<ProductSkuEntity> skuCaptor = ArgumentCaptor.forClass(ProductSkuEntity.class);
        verify(skuMapper).updateById(skuCaptor.capture());
        assertEquals("/cdn/images/AirPods Pro 2-sku.jpg", skuCaptor.getValue().getImageUrl());

        verify(imageMapper, org.mockito.Mockito.times(3)).insert(isA(ProductImageEntity.class));
        verify(esProductService).syncProductById(spuId);
    }

    @Test
    void bindProductImages_shouldRejectSkuImageWithoutSkuId() {
        Long spuId = 1002L;
        ProductSpuEntity spu = new ProductSpuEntity();
        spu.setId(spuId);
        spu.setShopId(100L);
        when(spuMapper.selectById(spuId)).thenReturn(spu);

        MerchantShopEntity shop = new MerchantShopEntity();
        shop.setId(100L);
        when(shopMapper.selectOne(any())).thenReturn(shop);

        ProductImageBindItemDTO skuImage = new ProductImageBindItemDTO();
        skuImage.setImageType(3);
        skuImage.setImageUrl("/cdn/images/invalid.jpg");

        ProductImageBindDTO dto = new ProductImageBindDTO();
        dto.setImages(List.of(skuImage));

        BusinessException ex = assertThrows(BusinessException.class, () -> productService.bindProductImages(spuId, dto));
        assertEquals("skuId is required for SKU images", ex.getMessage());

        verify(skuMapper, never()).updateById(isA(ProductSkuEntity.class));
        verify(esProductService, never()).syncProductById(anyLong());
    }
}




