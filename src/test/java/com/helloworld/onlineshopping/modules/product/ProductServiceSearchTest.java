package com.helloworld.onlineshopping.modules.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.modules.behavior.service.BrowseHistoryService;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.product.dto.ProductSearchDTO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductImageMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.product.search.EsProductService;
import com.helloworld.onlineshopping.modules.product.service.ProductService;
import com.helloworld.onlineshopping.modules.product.vo.ProductSimpleVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceSearchTest {

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

    @InjectMocks
    private ProductService productService;

    /**
     * 说明：当 ES 查询成功时，ProductService 应直接返回 ES 结果，不触发 DB 回退。
     * 样例：keyword=phone，ES 返回 1 条高亮商品（<em>phone</em> case）。
     */
    @Test
    void searchProducts_shouldReturnEsResult_whenEsAvailable() {
        ProductSearchDTO dto = new ProductSearchDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setKeyword("phone");

        ProductSimpleVO vo = new ProductSimpleVO();
        vo.setSpuId(101L);
        vo.setTitle("<em>phone</em> case");
        PageResult<ProductSimpleVO> esResult = PageResult.of(List.of(vo), 1, 1, 10);

        when(esProductService.searchProducts(dto)).thenReturn(esResult);

        PageResult<ProductSimpleVO> actual = productService.searchProducts(dto);

        assertSame(esResult, actual);
        verify(esProductService).searchProducts(dto);
        verify(spuMapper, never()).selectPage(any(Page.class), any());
    }

    /**
     * 说明：当 ES 抛出异常时，ProductService 应自动回退到 DB 查询，并完成店铺名称映射。
     * 样例：keyword=phone，price desc，price range=[100,2000]，DB 返回 Phone X。
     */
    @Test
    void searchProducts_shouldFallbackToDb_whenEsThrowsException() {
        ProductSearchDTO dto = new ProductSearchDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setKeyword("phone");
        dto.setSortField("price");
        dto.setSortOrder("desc");
        dto.setMinPrice(new BigDecimal("100"));
        dto.setMaxPrice(new BigDecimal("2000"));


    /**
     * 说明：当 ES 失败且传入复杂过滤参数时，服务应稳定走 DB 分支并正常返回分页结构。
     * 样例：keyword=phone，categoryId=9，brand=Apple，sort=sales。
     */
        when(esProductService.searchProducts(any(ProductSearchDTO.class)))
            .thenThrow(new RuntimeException("ES unavailable"));

        ProductSpuEntity spu = new ProductSpuEntity();
        spu.setId(1L);
        spu.setShopId(11L);
        spu.setTitle("Phone X");
        spu.setSubTitle("旗舰款");
        spu.setMainImage("img.png");
        spu.setMinPrice(new BigDecimal("999"));
        spu.setMaxPrice(new BigDecimal("1299"));
        spu.setSalesCount(88);

        Page<ProductSpuEntity> page = new Page<>(1, 10);
        page.setRecords(List.of(spu));
        page.setTotal(1);

        when(spuMapper.selectPage(any(Page.class), any())).thenReturn(page);

        MerchantShopEntity shop = new MerchantShopEntity();
        shop.setId(11L);
        shop.setShopName("Digital Store");
        when(shopMapper.selectById(11L)).thenReturn(shop);

        PageResult<ProductSimpleVO> result = productService.searchProducts(dto);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("Phone X", result.getList().get(0).getTitle());
        assertEquals("Digital Store", result.getList().get(0).getShopName());

        verify(esProductService).searchProducts(dto);
        verify(spuMapper).selectPage(any(Page.class), any());
        verify(shopMapper).selectById(11L);
    }

    @Test
    void searchProducts_shouldBuildDbWrapperWithFiltersAndSalesSort_whenEsFails() {
        ProductSearchDTO dto = new ProductSearchDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setKeyword("phone");
        dto.setCategoryId(9L);
        dto.setBrandName("Apple");
        dto.setMinPrice(new BigDecimal("500"));
        dto.setMaxPrice(new BigDecimal("3000"));
        dto.setSortField("sales");

        when(esProductService.searchProducts(any(ProductSearchDTO.class)))
            .thenThrow(new RuntimeException("ES unavailable"));

        Page<ProductSpuEntity> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductSpuEntity>> wrapperCaptor =
            ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class);
        when(spuMapper.selectPage(any(Page.class), wrapperCaptor.capture())).thenReturn(page);

        PageResult<ProductSimpleVO> result = productService.searchProducts(dto);

        assertEquals(0, result.getTotal());
        verify(esProductService).searchProducts(dto);
        verify(spuMapper).selectPage(any(Page.class), any());
    }

    /**
     * 说明：当 ES 失败且未指定排序字段时，服务应走默认排序分支（createTime desc）。
     * 样例：仅传 pageNum=1,pageSize=10，不传 sortField/sortOrder。
     */
    @Test
    void searchProducts_shouldUseCreateTimeDescByDefault_whenEsFailsAndNoSortField() {
        ProductSearchDTO dto = new ProductSearchDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        when(esProductService.searchProducts(any(ProductSearchDTO.class)))
            .thenThrow(new RuntimeException("ES unavailable"));

        Page<ProductSpuEntity> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductSpuEntity>> wrapperCaptor =
            ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class);
        when(spuMapper.selectPage(any(Page.class), wrapperCaptor.capture())).thenReturn(page);

        productService.searchProducts(dto);

        verify(esProductService).searchProducts(dto);
        verify(spuMapper).selectPage(any(Page.class), any());
    }
}
