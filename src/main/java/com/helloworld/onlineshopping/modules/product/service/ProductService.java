package com.helloworld.onlineshopping.modules.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.behavior.service.BrowseHistoryService;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.product.dto.ProductSearchDTO;
import com.helloworld.onlineshopping.modules.product.dto.ProductSkuDTO;
import com.helloworld.onlineshopping.modules.product.dto.ProductSpuCreateDTO;
import com.helloworld.onlineshopping.modules.product.dto.ProductSpuUpdateDTO;
import com.helloworld.onlineshopping.modules.product.entity.ProductImageEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductImageMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.product.vo.ProductDetailVO;
import com.helloworld.onlineshopping.modules.product.vo.ProductSimpleVO;
import com.helloworld.onlineshopping.modules.product.vo.ProductSkuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductSpuMapper spuMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductImageMapper imageMapper;
    private final MerchantShopMapper shopMapper;
    private final BrowseHistoryService browseHistoryService;

    @Transactional
    public void createProduct(ProductSpuCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null) {
            throw new BusinessException("You don't have a shop yet");
        }

        // Create SPU
        ProductSpuEntity spu = new ProductSpuEntity();
        spu.setShopId(shop.getId());
        spu.setCategoryId(dto.getCategoryId());
        spu.setBrandName(dto.getBrandName());
        spu.setTitle(dto.getTitle());
        spu.setSubTitle(dto.getSubTitle());
        spu.setMainImage(dto.getMainImage());
        spu.setDetailText(dto.getDetailText());
        spu.setStatus(0); // draft
        spu.setAuditStatus(0); // pending
        spu.setSalesCount(0);
        spu.setLikeCount(0);
        spu.setFavoriteCount(0);
        spu.setBrowseCount(0);

        // Calculate price range from SKUs
        if (dto.getSkuList() != null && !dto.getSkuList().isEmpty()) {
            BigDecimal minPrice = dto.getSkuList().stream()
                .map(ProductSkuDTO::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = dto.getSkuList().stream()
                .map(ProductSkuDTO::getPrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
            spu.setMinPrice(minPrice);
            spu.setMaxPrice(maxPrice);
        } else {
            spu.setMinPrice(BigDecimal.ZERO);
            spu.setMaxPrice(BigDecimal.ZERO);
        }

        spuMapper.insert(spu);

        // Create SKUs
        if (dto.getSkuList() != null) {
            for (ProductSkuDTO skuDTO : dto.getSkuList()) {
                ProductSkuEntity sku = new ProductSkuEntity();
                sku.setSpuId(spu.getId());
                sku.setSkuCode(skuDTO.getSkuCode() != null ? skuDTO.getSkuCode() : UUID.randomUUID().toString().substring(0, 16));
                sku.setSkuName(skuDTO.getSkuName());
                sku.setSpecJson(skuDTO.getSpecJson());
                sku.setSalePrice(skuDTO.getPrice());
                sku.setOriginPrice(skuDTO.getOriginPrice());
                sku.setStock(skuDTO.getStock());
                sku.setLockStock(0);
                sku.setWarningStock(10);
                sku.setImageUrl(skuDTO.getImageUrl());
                sku.setStatus(1);
                sku.setVersion(0);
                skuMapper.insert(sku);
            }
        }

        // Create images
        if (dto.getImageList() != null) {
            int order = 0;
            for (String imageUrl : dto.getImageList()) {
                ProductImageEntity image = new ProductImageEntity();
                image.setSpuId(spu.getId());
                image.setImageUrl(imageUrl);
                image.setImageType(1);
                image.setSortOrder(order++);
                image.setCreateTime(LocalDateTime.now());
                imageMapper.insert(image);
            }
        }
    }

    public PageResult<ProductSimpleVO> searchProducts(ProductSearchDTO dto) {
        Page<ProductSpuEntity> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<ProductSpuEntity> wrapper = new LambdaQueryWrapper<ProductSpuEntity>()
            .eq(ProductSpuEntity::getStatus, 1)
            .eq(ProductSpuEntity::getAuditStatus, 1);

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(ProductSpuEntity::getTitle, dto.getKeyword());
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(ProductSpuEntity::getCategoryId, dto.getCategoryId());
        }
        if (StringUtils.hasText(dto.getBrandName())) {
            wrapper.eq(ProductSpuEntity::getBrandName, dto.getBrandName());
        }
        if (dto.getMinPrice() != null) {
            wrapper.ge(ProductSpuEntity::getMinPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(ProductSpuEntity::getMaxPrice, dto.getMaxPrice());
        }

        // Sorting
        if ("price".equals(dto.getSortField())) {
            if ("desc".equalsIgnoreCase(dto.getSortOrder())) {
                wrapper.orderByDesc(ProductSpuEntity::getMinPrice);
            } else {
                wrapper.orderByAsc(ProductSpuEntity::getMinPrice);
            }
        } else if ("sales".equals(dto.getSortField())) {
            wrapper.orderByDesc(ProductSpuEntity::getSalesCount);
        } else {
            wrapper.orderByDesc(ProductSpuEntity::getCreateTime);
        }

        Page<ProductSpuEntity> result = spuMapper.selectPage(page, wrapper);
        List<ProductSimpleVO> voList = result.getRecords().stream().map(spu -> {
            ProductSimpleVO vo = new ProductSimpleVO();
            vo.setSpuId(spu.getId());
            vo.setTitle(spu.getTitle());
            vo.setSubTitle(spu.getSubTitle());
            vo.setMainImage(spu.getMainImage());
            vo.setMinPrice(spu.getMinPrice());
            vo.setMaxPrice(spu.getMaxPrice());
            vo.setSalesCount(spu.getSalesCount());
            MerchantShopEntity shop = shopMapper.selectById(spu.getShopId());
            vo.setShopName(shop != null ? shop.getShopName() : "");
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    @Cacheable(value = "product:detail", key = "#spuId")
    public ProductDetailVO getProductDetail(Long spuId) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("Product not found");
        }

        // Increment browse count
        spu.setBrowseCount(spu.getBrowseCount() + 1);
        spuMapper.updateById(spu);

        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.helloworld.onlineshopping.common.security.LoginUser loginUser) {
                browseHistoryService.recordBrowse(loginUser.getUserId(), spuId);
            }
        } catch (Exception ignored) {}

        ProductDetailVO vo = new ProductDetailVO();
        vo.setSpuId(spu.getId());
        vo.setTitle(spu.getTitle());
        vo.setSubTitle(spu.getSubTitle());
        vo.setBrandName(spu.getBrandName());
        vo.setMainImage(spu.getMainImage());
        vo.setDetailText(spu.getDetailText());
        vo.setStatus(spu.getStatus());
        vo.setShopId(spu.getShopId());
        vo.setMinPrice(spu.getMinPrice());
        vo.setMaxPrice(spu.getMaxPrice());
        vo.setSalesCount(spu.getSalesCount());
        vo.setFavoriteCount(spu.getFavoriteCount());

        MerchantShopEntity shop = shopMapper.selectById(spu.getShopId());
        vo.setShopName(shop != null ? shop.getShopName() : "");

        // Load images
        List<ProductImageEntity> images = imageMapper.selectList(
            new LambdaQueryWrapper<ProductImageEntity>()
                .eq(ProductImageEntity::getSpuId, spuId)
                .orderByAsc(ProductImageEntity::getSortOrder));
        vo.setImageList(images.stream().map(ProductImageEntity::getImageUrl).collect(Collectors.toList()));

        // Load SKUs
        List<ProductSkuEntity> skus = skuMapper.selectList(
            new LambdaQueryWrapper<ProductSkuEntity>()
                .eq(ProductSkuEntity::getSpuId, spuId)
                .eq(ProductSkuEntity::getStatus, 1));
        vo.setSkuList(skus.stream().map(sku -> {
            ProductSkuVO skuVO = new ProductSkuVO();
            skuVO.setSkuId(sku.getId());
            skuVO.setSkuCode(sku.getSkuCode());
            skuVO.setSkuName(sku.getSkuName());
            skuVO.setSpecJson(sku.getSpecJson());
            skuVO.setSalePrice(sku.getSalePrice());
            skuVO.setOriginPrice(sku.getOriginPrice());
            skuVO.setStock(sku.getStock());
            skuVO.setImageUrl(sku.getImageUrl());
            return skuVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    @CacheEvict(value = "product:detail", key = "#spuId")
    @Transactional
    public void updateProductStatus(Long spuId, Integer status) {
        Long userId = SecurityUtil.getCurrentUserId();
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("Product not found");
        }
        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null || !shop.getId().equals(spu.getShopId())) {
            throw new BusinessException("No permission to modify this product");
        }
        spu.setStatus(status);
        spuMapper.updateById(spu);
    }

    @CacheEvict(value = "product:detail", key = "#spuId")
    @Transactional
    public void updateProduct(Long spuId, ProductSpuUpdateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("Product not found");
        }

        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null || !shop.getId().equals(spu.getShopId())) {
            throw new BusinessException("No permission to modify this product");
        }

        // Update SPU basic info
        spu.setCategoryId(dto.getCategoryId());
        spu.setBrandName(dto.getBrandName());
        spu.setTitle(dto.getTitle());
        spu.setSubTitle(dto.getSubTitle());
        spu.setMainImage(dto.getMainImage());
        spu.setDetailText(dto.getDetailText());

        // Recalculate price range if SKUs provided
        if (dto.getSkuList() != null && !dto.getSkuList().isEmpty()) {
            BigDecimal minPrice = dto.getSkuList().stream()
                .map(ProductSkuDTO::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = dto.getSkuList().stream()
                .map(ProductSkuDTO::getPrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
            spu.setMinPrice(minPrice);
            spu.setMaxPrice(maxPrice);

            // Delete old SKUs and create new ones
            skuMapper.delete(new LambdaQueryWrapper<ProductSkuEntity>()
                .eq(ProductSkuEntity::getSpuId, spuId));

            for (ProductSkuDTO skuDTO : dto.getSkuList()) {
                ProductSkuEntity sku = new ProductSkuEntity();
                sku.setSpuId(spu.getId());
                sku.setSkuCode(skuDTO.getSkuCode() != null ? skuDTO.getSkuCode() : UUID.randomUUID().toString().substring(0, 16));
                sku.setSkuName(skuDTO.getSkuName());
                sku.setSpecJson(skuDTO.getSpecJson());
                sku.setSalePrice(skuDTO.getPrice());
                sku.setOriginPrice(skuDTO.getOriginPrice());
                sku.setStock(skuDTO.getStock());
                sku.setLockStock(0);
                sku.setWarningStock(10);
                sku.setImageUrl(skuDTO.getImageUrl());
                sku.setStatus(1);
                sku.setVersion(0);
                skuMapper.insert(sku);
            }
        }

        spuMapper.updateById(spu);

        // Update images if provided
        if (dto.getImageList() != null) {
            imageMapper.delete(new LambdaQueryWrapper<ProductImageEntity>()
                .eq(ProductImageEntity::getSpuId, spuId));

            int order = 0;
            for (String imageUrl : dto.getImageList()) {
                ProductImageEntity image = new ProductImageEntity();
                image.setSpuId(spu.getId());
                image.setImageUrl(imageUrl);
                image.setImageType(1);
                image.setSortOrder(order++);
                image.setCreateTime(LocalDateTime.now());
                imageMapper.insert(image);
            }
        }
    }

    public PageResult<ProductSimpleVO> getMyProducts(Integer pageNum, Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null) {
            throw new BusinessException("You don't have a shop yet");
        }

        Page<ProductSpuEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductSpuEntity> wrapper = new LambdaQueryWrapper<ProductSpuEntity>()
            .eq(ProductSpuEntity::getShopId, shop.getId())
            .orderByDesc(ProductSpuEntity::getCreateTime);

        Page<ProductSpuEntity> result = spuMapper.selectPage(page, wrapper);
        List<ProductSimpleVO> voList = result.getRecords().stream().map(spu -> {
            ProductSimpleVO vo = new ProductSimpleVO();
            vo.setSpuId(spu.getId());
            vo.setTitle(spu.getTitle());
            vo.setSubTitle(spu.getSubTitle());
            vo.setMainImage(spu.getMainImage());
            vo.setMinPrice(spu.getMinPrice());
            vo.setMaxPrice(spu.getMaxPrice());
            vo.setSalesCount(spu.getSalesCount());
            vo.setShopName(shop.getShopName());
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), pageNum, pageSize);
    }

    @CacheEvict(value = "product:detail", key = "#spuId")
    @Transactional
    public void deleteProduct(Long spuId) {
        Long userId = SecurityUtil.getCurrentUserId();
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("Product not found");
        }

        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null || !shop.getId().equals(spu.getShopId())) {
            throw new BusinessException("No permission to delete this product");
        }

        // Soft delete SPU
        spu.setDeleted(1);
        spuMapper.updateById(spu);

        // Soft delete associated SKUs
        List<ProductSkuEntity> skus = skuMapper.selectList(
            new LambdaQueryWrapper<ProductSkuEntity>().eq(ProductSkuEntity::getSpuId, spuId));
        for (ProductSkuEntity sku : skus) {
            sku.setDeleted(1);
            skuMapper.updateById(sku);
        }
    }
}
