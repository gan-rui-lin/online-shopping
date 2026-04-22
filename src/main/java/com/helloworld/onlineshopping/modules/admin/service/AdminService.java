package com.helloworld.onlineshopping.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.admin.dto.AdminUserQueryDTO;
import com.helloworld.onlineshopping.modules.admin.vo.DashboardVO;
import com.helloworld.onlineshopping.modules.admin.vo.AdminUserVO;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.order.entity.OrderEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.user.entity.RoleEntity;
import com.helloworld.onlineshopping.modules.user.entity.UserEntity;
import com.helloworld.onlineshopping.modules.user.entity.UserRoleEntity;
import com.helloworld.onlineshopping.modules.user.mapper.RoleMapper;
import com.helloworld.onlineshopping.modules.user.mapper.UserMapper;
import com.helloworld.onlineshopping.modules.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final MerchantShopMapper shopMapper;
    private final ProductSpuMapper spuMapper;
    private final OrderMapper orderMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setMerchantCount(shopMapper.selectCount(null));
        vo.setProductCount(spuMapper.selectCount(
            new LambdaQueryWrapper<ProductSpuEntity>().eq(ProductSpuEntity::getStatus, 1)));
        vo.setOrderCount(orderMapper.selectCount(null));

        // Calculate GMV (sum of paid orders)
        List<OrderEntity> paidOrders = orderMapper.selectList(
            new LambdaQueryWrapper<OrderEntity>().ne(OrderEntity::getPayStatus, 0));
        BigDecimal gmv = paidOrders.stream()
            .map(OrderEntity::getPayAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setGmv(gmv);

        // Today's order count
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayOrderCount(orderMapper.selectCount(
            new LambdaQueryWrapper<OrderEntity>().ge(OrderEntity::getCreateTime, todayStart)));

        return vo;
    }

    public PageResult<AdminUserVO> getUsers(AdminUserQueryDTO dto) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            wrapper.and(w -> w.like(UserEntity::getUsername, keyword)
                .or().like(UserEntity::getNickname, keyword)
                .or().like(UserEntity::getPhone, keyword)
                .or().like(UserEntity::getEmail, keyword));
        }
        if (dto.getStatus() != null) {
            wrapper.eq(UserEntity::getStatus, dto.getStatus());
        }
        if (dto.getUserType() != null) {
            wrapper.eq(UserEntity::getUserType, dto.getUserType());
        }
        wrapper.orderByDesc(UserEntity::getCreateTime);

        Page<UserEntity> page = userMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        if (page.getRecords().isEmpty()) {
            return PageResult.of(List.of(), page.getTotal(), dto.getPageNum(), dto.getPageSize());
        }

        List<Long> userIds = page.getRecords().stream().map(UserEntity::getId).toList();
        Map<Long, List<String>> userRoleMap = loadUserRoleCodes(userIds);
        List<AdminUserVO> list = page.getRecords().stream().map(user -> {
            AdminUserVO vo = new AdminUserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setStatus(user.getStatus());
            vo.setUserType(user.getUserType());
            vo.setLastLoginTime(user.getLastLoginTime());
            vo.setCreateTime(user.getCreateTime());
            vo.setRoles(userRoleMap.getOrDefault(user.getId(), List.of()));
            return vo;
        }).toList();

        return PageResult.of(list, page.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    public void updateUserStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("Invalid status value");
        }

        Long operatorId = SecurityUtil.getCurrentUserId();
        if (userId.equals(operatorId)) {
            throw new BusinessException("You cannot disable your own account");
        }

        UserEntity target = userMapper.selectById(userId);
        if (target == null) {
            throw new BusinessException("User not found");
        }
        if (target.getStatus().equals(status)) {
            return;
        }
        if (target.getUserType() != null && target.getUserType() == 3 && status == 0) {
            long activeAdminCount = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserType, 3)
                .eq(UserEntity::getStatus, 1));
            if (activeAdminCount <= 1) {
                throw new BusinessException("At least one active admin account must remain");
            }
        }

        target.setStatus(status);
        userMapper.updateById(target);
    }

    private Map<Long, List<String>> loadUserRoleCodes(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UserRoleEntity> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRoleEntity>().in(UserRoleEntity::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toSet());
        Map<Long, String> roleCodeMap = roleMapper.selectBatchIds(roleIds).stream()
            .collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getRoleCode));

        Map<Long, List<String>> userRoleMap = new HashMap<>();
        for (UserRoleEntity userRole : userRoles) {
            String roleCode = roleCodeMap.get(userRole.getRoleId());
            if (roleCode == null) {
                continue;
            }
            userRoleMap.computeIfAbsent(userRole.getUserId(), k -> new java.util.ArrayList<>()).add(roleCode);
        }
        return userRoleMap;
    }
}
