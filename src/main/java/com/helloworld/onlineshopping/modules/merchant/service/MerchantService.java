package com.helloworld.onlineshopping.modules.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.merchant.dto.MerchantApplyDTO;
import com.helloworld.onlineshopping.modules.merchant.dto.MerchantAuditDTO;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantApplyEntity;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantApplyMapper;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.merchant.vo.MerchantApplyVO;
import com.helloworld.onlineshopping.modules.merchant.vo.MerchantShopVO;
import com.helloworld.onlineshopping.modules.user.entity.UserEntity;
import com.helloworld.onlineshopping.modules.user.entity.UserRoleEntity;
import com.helloworld.onlineshopping.modules.user.entity.RoleEntity;
import com.helloworld.onlineshopping.modules.user.mapper.UserMapper;
import com.helloworld.onlineshopping.modules.user.mapper.UserRoleMapper;
import com.helloworld.onlineshopping.modules.user.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantApplyMapper applyMapper;
    private final MerchantShopMapper shopMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    public void apply(MerchantApplyDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        // Check if already has pending application
        Long pendingCount = applyMapper.selectCount(
            new LambdaQueryWrapper<MerchantApplyEntity>()
                .eq(MerchantApplyEntity::getUserId, userId)
                .eq(MerchantApplyEntity::getApplyStatus, 0));
        if (pendingCount > 0) {
            throw new BusinessException("You already have a pending application");
        }

        // Check if already a merchant
        MerchantShopEntity existingShop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (existingShop != null) {
            throw new BusinessException("You already have a shop");
        }

        MerchantApplyEntity apply = new MerchantApplyEntity();
        apply.setUserId(userId);
        apply.setShopName(dto.getShopName());
        apply.setBusinessLicenseNo(dto.getBusinessLicenseNo());
        apply.setContactName(dto.getContactName());
        apply.setContactPhone(dto.getContactPhone());
        apply.setApplyStatus(0);
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        applyMapper.insert(apply);
    }

    @Transactional
    public void audit(Long applyId, MerchantAuditDTO dto) {
        Long adminId = SecurityUtil.getCurrentUserId();
        MerchantApplyEntity apply = applyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("Application not found");
        }
        if (apply.getApplyStatus() != 0) {
            throw new BusinessException("Application already processed");
        }

        apply.setApplyStatus(dto.getAuditStatus());
        apply.setRemark(dto.getRemark());
        apply.setAuditBy(adminId);
        apply.setAuditTime(LocalDateTime.now());
        applyMapper.updateById(apply);

        // If approved, create shop and assign merchant role
        if (dto.getAuditStatus() == 1) {
            MerchantShopEntity shop = new MerchantShopEntity();
            shop.setUserId(apply.getUserId());
            shop.setShopName(apply.getShopName());
            shop.setShopStatus(1);
            shop.setScore(new BigDecimal("5.00"));
            shopMapper.insert(shop);

            // Update user type
            UserEntity user = userMapper.selectById(apply.getUserId());
            if (user != null) {
                user.setUserType(2);
                userMapper.updateById(user);
            }

            // Assign merchant role
            RoleEntity merchantRole = roleMapper.selectOne(
                new LambdaQueryWrapper<RoleEntity>().eq(RoleEntity::getRoleCode, "ROLE_MERCHANT"));
            if (merchantRole != null) {
                Long existCount = userRoleMapper.selectCount(
                    new LambdaQueryWrapper<UserRoleEntity>()
                        .eq(UserRoleEntity::getUserId, apply.getUserId())
                        .eq(UserRoleEntity::getRoleId, merchantRole.getId()));
                if (existCount == 0) {
                    UserRoleEntity userRole = new UserRoleEntity();
                    userRole.setUserId(apply.getUserId());
                    userRole.setRoleId(merchantRole.getId());
                    userRole.setCreateTime(LocalDateTime.now());
                    userRoleMapper.insert(userRole);
                }
            }
        }
    }

    public MerchantShopVO getCurrentShop() {
        Long userId = SecurityUtil.getCurrentUserId();
        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null) {
            throw new BusinessException("Shop not found");
        }
        return toShopVO(shop);
    }

    public List<MerchantApplyVO> getPendingApplyList() {
        List<MerchantApplyEntity> list = applyMapper.selectList(
            new LambdaQueryWrapper<MerchantApplyEntity>()
                .eq(MerchantApplyEntity::getApplyStatus, 0)
                .orderByDesc(MerchantApplyEntity::getCreateTime));
        return list.stream().map(apply -> {
            MerchantApplyVO vo = new MerchantApplyVO();
            vo.setId(apply.getId());
            vo.setUserId(apply.getUserId());
            vo.setShopName(apply.getShopName());
            vo.setBusinessLicenseNo(apply.getBusinessLicenseNo());
            vo.setContactName(apply.getContactName());
            vo.setContactPhone(apply.getContactPhone());
            vo.setApplyStatus(apply.getApplyStatus());
            vo.setRemark(apply.getRemark());
            vo.setCreateTime(apply.getCreateTime());
            UserEntity user = userMapper.selectById(apply.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private MerchantShopVO toShopVO(MerchantShopEntity entity) {
        MerchantShopVO vo = new MerchantShopVO();
        vo.setShopId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setShopName(entity.getShopName());
        vo.setShopLogo(entity.getShopLogo());
        vo.setShopDesc(entity.getShopDesc());
        vo.setShopStatus(entity.getShopStatus());
        vo.setScore(entity.getScore());
        return vo;
    }
}
