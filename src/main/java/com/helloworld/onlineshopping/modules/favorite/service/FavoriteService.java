package com.helloworld.onlineshopping.modules.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.favorite.entity.UserFavoriteEntity;
import com.helloworld.onlineshopping.modules.favorite.mapper.UserFavoriteMapper;
import com.helloworld.onlineshopping.modules.favorite.vo.FavoriteVO;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final UserFavoriteMapper favoriteMapper;
    private final ProductSpuMapper spuMapper;
    private final MerchantShopMapper shopMapper;

    @Transactional
    public boolean toggleFavorite(Long spuId) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserFavoriteEntity existing = favoriteMapper.selectOne(
            new LambdaQueryWrapper<UserFavoriteEntity>()
                .eq(UserFavoriteEntity::getUserId, userId)
                .eq(UserFavoriteEntity::getSpuId, spuId));
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            if (spu != null && spu.getFavoriteCount() > 0) {
                spu.setFavoriteCount(spu.getFavoriteCount() - 1);
                spuMapper.updateById(spu);
            }
            return false;
        } else {
            UserFavoriteEntity entity = new UserFavoriteEntity();
            entity.setUserId(userId);
            entity.setSpuId(spuId);
            entity.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(entity);
            if (spu != null) {
                spu.setFavoriteCount(spu.getFavoriteCount() + 1);
                spuMapper.updateById(spu);
            }
            return true;
        }
    }

    public PageResult<FavoriteVO> getFavoriteList(int pageNum, int pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<UserFavoriteEntity> page = new Page<>(pageNum, pageSize);
        Page<UserFavoriteEntity> result = favoriteMapper.selectPage(page,
            new LambdaQueryWrapper<UserFavoriteEntity>()
                .eq(UserFavoriteEntity::getUserId, userId)
                .orderByDesc(UserFavoriteEntity::getCreateTime));
        List<FavoriteVO> voList = result.getRecords().stream().map(fav -> {
            FavoriteVO vo = new FavoriteVO();
            vo.setSpuId(fav.getSpuId());
            vo.setCreateTime(fav.getCreateTime());
            ProductSpuEntity spu = spuMapper.selectById(fav.getSpuId());
            if (spu != null) {
                vo.setTitle(spu.getTitle());
                vo.setMainImage(spu.getMainImage());
                vo.setMinPrice(spu.getMinPrice());
                MerchantShopEntity shop = shopMapper.selectById(spu.getShopId());
                vo.setShopName(shop != null ? shop.getShopName() : "");
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), pageNum, pageSize);
    }

    public boolean isFavorited(Long spuId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return favoriteMapper.selectCount(
            new LambdaQueryWrapper<UserFavoriteEntity>()
                .eq(UserFavoriteEntity::getUserId, userId)
                .eq(UserFavoriteEntity::getSpuId, spuId)) > 0;
    }
}
