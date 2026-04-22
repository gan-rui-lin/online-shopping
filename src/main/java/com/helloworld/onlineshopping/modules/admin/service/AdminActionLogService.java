package com.helloworld.onlineshopping.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.modules.admin.dto.AdminActionLogQueryDTO;
import com.helloworld.onlineshopping.modules.admin.entity.AdminActionLogEntity;
import com.helloworld.onlineshopping.modules.admin.mapper.AdminActionLogMapper;
import com.helloworld.onlineshopping.modules.admin.vo.AdminActionLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminActionLogService {

    private final AdminActionLogMapper adminActionLogMapper;

    public void record(Long operatorId, String operatorName, String module, String action,
                       String targetType, String targetId, String detail, boolean success) {
        try {
            AdminActionLogEntity entity = new AdminActionLogEntity();
            entity.setOperatorId(operatorId);
            entity.setOperatorName(operatorName);
            entity.setModule(module);
            entity.setAction(action);
            entity.setTargetType(targetType);
            entity.setTargetId(targetId);
            entity.setDetail(detail);
            entity.setSuccess(success ? 1 : 0);
            entity.setCreateTime(LocalDateTime.now());
            adminActionLogMapper.insert(entity);
        } catch (Exception ignored) {
            // do not break the business request when log persistence fails
        }
    }

    public PageResult<AdminActionLogVO> list(AdminActionLogQueryDTO dto) {
        LambdaQueryWrapper<AdminActionLogEntity> wrapper = new LambdaQueryWrapper<>();
        if (dto.getOperatorId() != null) {
            wrapper.eq(AdminActionLogEntity::getOperatorId, dto.getOperatorId());
        }
        if (StringUtils.hasText(dto.getModule())) {
            wrapper.eq(AdminActionLogEntity::getModule, dto.getModule().trim());
        }
        if (dto.getSuccess() != null) {
            wrapper.eq(AdminActionLogEntity::getSuccess, dto.getSuccess());
        }
        wrapper.orderByDesc(AdminActionLogEntity::getCreateTime);

        Page<AdminActionLogEntity> page = adminActionLogMapper.selectPage(
            new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<AdminActionLogVO> list = page.getRecords().stream().map(record -> {
            AdminActionLogVO vo = new AdminActionLogVO();
            vo.setId(record.getId());
            vo.setOperatorId(record.getOperatorId());
            vo.setOperatorName(record.getOperatorName());
            vo.setModule(record.getModule());
            vo.setAction(record.getAction());
            vo.setTargetType(record.getTargetType());
            vo.setTargetId(record.getTargetId());
            vo.setDetail(record.getDetail());
            vo.setSuccess(record.getSuccess());
            vo.setCreateTime(record.getCreateTime());
            return vo;
        }).toList();

        return PageResult.of(list, page.getTotal(), dto.getPageNum(), dto.getPageSize());
    }
}
