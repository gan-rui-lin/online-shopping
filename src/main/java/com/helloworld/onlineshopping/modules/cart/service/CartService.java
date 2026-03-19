package com.helloworld.onlineshopping.modules.cart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.cart.dto.CartAddDTO;
import com.helloworld.onlineshopping.modules.cart.dto.CartUpdateDTO;
import com.helloworld.onlineshopping.modules.cart.entity.CartItemEntity;
import com.helloworld.onlineshopping.modules.cart.mapper.CartItemMapper;
import com.helloworld.onlineshopping.modules.cart.vo.CartItemVO;
import com.helloworld.onlineshopping.modules.cart.vo.CartVO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductSpuMapper spuMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CART_KEY_PREFIX = "cart:user:";

    public void addItem(CartAddDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        // Verify SKU exists and has stock
        ProductSkuEntity sku = skuMapper.selectById(dto.getSkuId());
        if (sku == null) {
            throw new BusinessException("Product SKU not found");
        }
        if (sku.getStock() < dto.getQuantity()) {
            throw new BusinessException("Insufficient stock");
        }

        // Check if item already in cart
        CartItemEntity existing = cartItemMapper.selectOne(
            new LambdaQueryWrapper<CartItemEntity>()
                .eq(CartItemEntity::getUserId, userId)
                .eq(CartItemEntity::getSkuId, dto.getSkuId()));

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            cartItemMapper.updateById(existing);
        } else {
            CartItemEntity item = new CartItemEntity();
            item.setUserId(userId);
            item.setSkuId(dto.getSkuId());
            item.setQuantity(dto.getQuantity());
            item.setChecked(1);
            cartItemMapper.insert(item);
        }

        // Invalidate Redis cache
        redisTemplate.delete(CART_KEY_PREFIX + userId);
    }

    public CartVO getCartList() {
        Long userId = SecurityUtil.getCurrentUserId();

        List<CartItemEntity> items = cartItemMapper.selectList(
            new LambdaQueryWrapper<CartItemEntity>()
                .eq(CartItemEntity::getUserId, userId)
                .orderByDesc(CartItemEntity::getCreateTime));

        CartVO cart = new CartVO();
        List<CartItemVO> voList = new ArrayList<>();
        int totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        int checkedCount = 0;
        BigDecimal checkedAmount = BigDecimal.ZERO;

        if (items.isEmpty()) {
            cart.setItems(voList);
            cart.setTotalCount(0);
            cart.setTotalAmount(BigDecimal.ZERO);
            cart.setCheckedCount(0);
            cart.setCheckedAmount(BigDecimal.ZERO);
            return cart;
        }

        // Batch load SKUs
        List<Long> skuIds = items.stream().map(CartItemEntity::getSkuId).distinct().collect(java.util.stream.Collectors.toList());
        List<ProductSkuEntity> skuEntities = skuMapper.selectBatchIds(skuIds);
        java.util.Map<Long, ProductSkuEntity> skuMap = skuEntities.stream().collect(java.util.stream.Collectors.toMap(ProductSkuEntity::getId, s -> s));

        // Batch load SPUs
        List<Long> spuIds = skuEntities.stream().map(ProductSkuEntity::getSpuId).distinct().collect(java.util.stream.Collectors.toList());
        List<ProductSpuEntity> spuEntities = spuMapper.selectBatchIds(spuIds);
        java.util.Map<Long, ProductSpuEntity> spuMap = spuEntities.stream().collect(java.util.stream.Collectors.toMap(ProductSpuEntity::getId, s -> s));

        for (CartItemEntity item : items) {
            ProductSkuEntity sku = skuMap.get(item.getSkuId());
            if (sku == null) continue;
            ProductSpuEntity spu = spuMap.get(sku.getSpuId());
            if (spu == null) continue;

            CartItemVO vo = new CartItemVO();
            vo.setSkuId(sku.getId());
            vo.setSpuId(spu.getId());
            vo.setSpuName(spu.getTitle());
            vo.setSkuName(sku.getSkuName());
            vo.setMainImage(sku.getImageUrl() != null ? sku.getImageUrl() : spu.getMainImage());
            vo.setSpecJson(sku.getSpecJson());
            vo.setPrice(sku.getSalePrice());
            vo.setQuantity(item.getQuantity());
            vo.setChecked(item.getChecked());
            vo.setStock(sku.getStock());
            vo.setAvailable(sku.getStatus() == 1 && spu.getStatus() == 1);
            voList.add(vo);

            BigDecimal itemTotal = sku.getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalCount += item.getQuantity();
            totalAmount = totalAmount.add(itemTotal);
            if (item.getChecked() == 1) {
                checkedCount += item.getQuantity();
                checkedAmount = checkedAmount.add(itemTotal);
            }
        }

        cart.setItems(voList);
        cart.setTotalCount(totalCount);
        cart.setTotalAmount(totalAmount);
        cart.setCheckedCount(checkedCount);
        cart.setCheckedAmount(checkedAmount);
        return cart;
    }

    public void updateItem(CartUpdateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        CartItemEntity item = cartItemMapper.selectOne(
            new LambdaQueryWrapper<CartItemEntity>()
                .eq(CartItemEntity::getUserId, userId)
                .eq(CartItemEntity::getSkuId, dto.getSkuId()));
        if (item == null) {
            throw new BusinessException("Cart item not found");
        }
        if (dto.getQuantity() != null) {
            item.setQuantity(dto.getQuantity());
        }
        if (dto.getChecked() != null) {
            item.setChecked(dto.getChecked());
        }
        cartItemMapper.updateById(item);
        redisTemplate.delete(CART_KEY_PREFIX + userId);
    }

    public void removeItem(Long skuId) {
        Long userId = SecurityUtil.getCurrentUserId();
        cartItemMapper.delete(
            new LambdaQueryWrapper<CartItemEntity>()
                .eq(CartItemEntity::getUserId, userId)
                .eq(CartItemEntity::getSkuId, skuId));
        redisTemplate.delete(CART_KEY_PREFIX + userId);
    }

    public void removeCheckedItems(Long userId, List<Long> skuIds) {
        cartItemMapper.delete(
            new LambdaQueryWrapper<CartItemEntity>()
                .eq(CartItemEntity::getUserId, userId)
                .in(CartItemEntity::getSkuId, skuIds));
        redisTemplate.delete(CART_KEY_PREFIX + userId);
    }
}
