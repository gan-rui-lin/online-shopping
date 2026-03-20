package com.helloworld.onlineshopping.modules.product.search;

import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.product.dto.ProductSearchDTO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.product.vo.ProductSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsProductService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final EsProductRepository esProductRepository;
    private final ProductSpuMapper productSpuMapper;
    private final MerchantShopMapper merchantShopMapper;

    public PageResult<ProductSimpleVO> searchProducts(ProductSearchDTO dto) {
        NativeQueryBuilderHelper helper = new NativeQueryBuilderHelper(dto);
        NativeQuery query = helper.build();

        SearchHits<EsProductDocument> hits = elasticsearchOperations.search(query, EsProductDocument.class);
        List<ProductSimpleVO> list = hits.getSearchHits().stream()
            .map(hit -> toSimpleVO(hit, dto.getKeyword()))
            .collect(Collectors.toList());

        return PageResult.of(list, hits.getTotalHits(), dto.getPageNum(), dto.getPageSize());
    }

    public void syncProduct(ProductSpuEntity spu) {
        if (spu == null || Objects.equals(spu.getDeleted(), 1)) {
            return;
        }
        EsProductDocument doc = new EsProductDocument();
        doc.setSpuId(spu.getId());
        doc.setShopId(spu.getShopId());
        doc.setCategoryId(spu.getCategoryId());
        doc.setTitle(spu.getTitle());
        doc.setSubTitle(spu.getSubTitle());
        doc.setBrandName(spu.getBrandName());
        doc.setMainImage(spu.getMainImage());
        doc.setMinPrice(spu.getMinPrice());
        doc.setMaxPrice(spu.getMaxPrice());
        doc.setSalesCount(spu.getSalesCount());
        doc.setStatus(spu.getStatus());
        doc.setAuditStatus(spu.getAuditStatus());
        doc.setCreateTime(spu.getCreateTime());
        esProductRepository.save(doc);
    }

    public void syncProductById(Long spuId) {
        ProductSpuEntity spu = productSpuMapper.selectById(spuId);
        if (spu == null || Objects.equals(spu.getDeleted(), 1)) {
            deleteProduct(spuId);
            return;
        }
        syncProduct(spu);
    }

    public void deleteProduct(Long spuId) {
        esProductRepository.deleteById(spuId);
    }

    private ProductSimpleVO toSimpleVO(SearchHit<EsProductDocument> hit, String keyword) {
        EsProductDocument doc = hit.getContent();
        ProductSimpleVO vo = new ProductSimpleVO();
        vo.setSpuId(doc.getSpuId());
        vo.setTitle(resolveTitleWithHighlight(hit, doc.getTitle(), keyword));
        vo.setSubTitle(doc.getSubTitle());
        vo.setMainImage(doc.getMainImage());
        vo.setMinPrice(doc.getMinPrice());
        vo.setMaxPrice(doc.getMaxPrice());
        vo.setSalesCount(doc.getSalesCount());

        MerchantShopEntity shop = merchantShopMapper.selectById(doc.getShopId());
        vo.setShopName(shop != null ? shop.getShopName() : "");
        return vo;
    }

    private String resolveTitleWithHighlight(SearchHit<EsProductDocument> hit, String origin, String keyword) {
        Map<String, List<String>> highlightFields = hit.getHighlightFields();
        List<String> titleHighlights = highlightFields != null ? highlightFields.get("title") : null;
        if (titleHighlights != null && !titleHighlights.isEmpty()) {
            return titleHighlights.get(0);
        }
        if (StringUtils.hasText(keyword) && StringUtils.hasText(origin)) {
            return origin.replace(keyword, "<em>" + keyword + "</em>");
        }
        return origin;
    }

    private static class NativeQueryBuilderHelper {

        private final ProductSearchDTO dto;

        private NativeQueryBuilderHelper(ProductSearchDTO dto) {
            this.dto = dto;
        }

        private NativeQuery build() {
            NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    b.must(m -> m.term(t -> t.field("status").value(1)));
                    b.must(m -> m.term(t -> t.field("auditStatus").value(1)));

                    if (StringUtils.hasText(dto.getKeyword())) {
                        b.must(m -> m.multiMatch(mm -> mm
                            .query(dto.getKeyword())
                            .fields("title^3", "subTitle^2", "brandName")));
                    }
                    if (dto.getCategoryId() != null) {
                        b.filter(f -> f.term(t -> t.field("categoryId").value(dto.getCategoryId())));
                    }
                    if (StringUtils.hasText(dto.getBrandName())) {
                        b.filter(f -> f.term(t -> t.field("brandName").value(dto.getBrandName())));
                    }
                    if (dto.getMinPrice() != null || dto.getMaxPrice() != null) {
                        b.filter(f -> f.range(r -> {
                            r.field("minPrice");
                            if (dto.getMinPrice() != null) {
                                r.gte(co.elastic.clients.json.JsonData.of(dto.getMinPrice()));
                            }
                            if (dto.getMaxPrice() != null) {
                                r.lte(co.elastic.clients.json.JsonData.of(dto.getMaxPrice()));
                            }
                            return r;
                        }));
                    }
                    return b;
                }))
                .withPageable(PageRequest.of(dto.getPageNum() - 1, dto.getPageSize()))
                .withTrackTotalHits(true)
                .build();

            if ("price".equals(dto.getSortField())) {
                return NativeQuery.builder()
                    .withQuery(query.getQuery())
                    .withPageable(query.getPageable())
                    .withTrackTotalHits(true)
                    .withSort(s -> s.field(f -> f
                        .field("minPrice")
                        .order("desc".equalsIgnoreCase(dto.getSortOrder())
                            ? co.elastic.clients.elasticsearch._types.SortOrder.Desc
                            : co.elastic.clients.elasticsearch._types.SortOrder.Asc)))
                    .build();
            }

            if ("sales".equals(dto.getSortField())) {
                return NativeQuery.builder()
                    .withQuery(query.getQuery())
                    .withPageable(query.getPageable())
                    .withTrackTotalHits(true)
                    .withSort(s -> s.field(f -> f
                        .field("salesCount")
                        .order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                    .build();
            }

            return NativeQuery.builder()
                .withQuery(query.getQuery())
                .withPageable(query.getPageable())
                .withTrackTotalHits(true)
                .withSort(s -> s.field(f -> f
                    .field("createTime")
                    .order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                .build();
        }
    }
}
