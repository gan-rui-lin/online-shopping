package com.helloworld.onlineshopping.modules.behavior.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.behavior.entity.UserBrowseHistoryEntity;
import com.helloworld.onlineshopping.modules.behavior.mapper.UserBrowseHistoryMapper;
import com.helloworld.onlineshopping.modules.behavior.vo.BrowseHistoryVO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrowseHistoryService {
    private final UserBrowseHistoryMapper historyMapper;
    private final ProductSpuMapper spuMapper;

    public void recordBrowse(Long userId, Long spuId) {
        UserBrowseHistoryEntity entity = new UserBrowseHistoryEntity();
        entity.setUserId(userId);
        entity.setSpuId(spuId);
        entity.setBrowseTime(LocalDateTime.now());
        historyMapper.insert(entity);
    }

    public PageResult<BrowseHistoryVO> getBrowseHistory(int pageNum, int pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<UserBrowseHistoryEntity> page = new Page<>(pageNum, pageSize);
        Page<UserBrowseHistoryEntity> result = historyMapper.selectPage(page,
            new LambdaQueryWrapper<UserBrowseHistoryEntity>()
                .eq(UserBrowseHistoryEntity::getUserId, userId)
                .orderByDesc(UserBrowseHistoryEntity::getBrowseTime));
        List<BrowseHistoryVO> voList = result.getRecords().stream().map(h -> {
            BrowseHistoryVO vo = new BrowseHistoryVO();
            vo.setSpuId(h.getSpuId());
            vo.setBrowseTime(h.getBrowseTime());
            ProductSpuEntity spu = spuMapper.selectById(h.getSpuId());
            if (spu != null) {
                vo.setTitle(spu.getTitle());
                vo.setMainImage(spu.getMainImage());
                vo.setMinPrice(spu.getMinPrice());
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), pageNum, pageSize);
    }

    public void clearHistory() {
        Long userId = SecurityUtil.getCurrentUserId();
        historyMapper.delete(new LambdaQueryWrapper<UserBrowseHistoryEntity>()
            .eq(UserBrowseHistoryEntity::getUserId, userId));
    }
}
