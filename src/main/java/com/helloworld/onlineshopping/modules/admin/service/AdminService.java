package com.helloworld.onlineshopping.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.admin.dto.AdminOrderQueryDTO;
import com.helloworld.onlineshopping.modules.admin.dto.AdminUserQueryDTO;
import com.helloworld.onlineshopping.modules.admin.vo.AdminOrderVO;
import com.helloworld.onlineshopping.modules.admin.vo.AdminUserVO;
import com.helloworld.onlineshopping.modules.admin.vo.DashboardTrendVO;
import com.helloworld.onlineshopping.modules.admin.vo.DashboardVO;
import com.helloworld.onlineshopping.modules.admin.vo.OrderStatusStatVO;
import com.helloworld.onlineshopping.modules.admin.vo.SecurityOverviewVO;
import com.helloworld.onlineshopping.modules.auth.service.LoginSecurityService;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.order.entity.OrderEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderMapper;
import com.helloworld.onlineshopping.modules.order.service.OrderService;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.user.entity.RoleEntity;
import com.helloworld.onlineshopping.modules.user.entity.UserEntity;
import com.helloworld.onlineshopping.modules.user.entity.UserRoleEntity;
import com.helloworld.onlineshopping.modules.user.mapper.RoleMapper;
import com.helloworld.onlineshopping.modules.user.mapper.UserMapper;
import com.helloworld.onlineshopping.modules.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private final OrderService orderService;
    private final LoginSecurityService loginSecurityService;

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

        // Last 7-day trend
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDateTime trendStart = LocalDateTime.of(startDate, LocalTime.MIN);
        List<OrderEntity> trendOrders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
            .ge(OrderEntity::getCreateTime, trendStart));

        Map<LocalDate, Long> orderCountMap = trendOrders.stream()
            .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate(), Collectors.counting()));
        Map<LocalDate, BigDecimal> gmvByDateMap = trendOrders.stream()
            .filter(o -> o.getPayStatus() != null && o.getPayStatus() != 0)
            .collect(Collectors.groupingBy(
                o -> o.getCreateTime().toLocalDate(),
                Collectors.reducing(BigDecimal.ZERO, OrderEntity::getPayAmount, BigDecimal::add)));

        List<DashboardTrendVO> orderTrend = new java.util.ArrayList<>();
        List<DashboardTrendVO> gmvTrend = new java.util.ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);
            DashboardTrendVO orderPoint = new DashboardTrendVO();
            orderPoint.setDate(day.toString());
            orderPoint.setValue(BigDecimal.valueOf(orderCountMap.getOrDefault(day, 0L)));
            orderTrend.add(orderPoint);

            DashboardTrendVO gmvPoint = new DashboardTrendVO();
            gmvPoint.setDate(day.toString());
            gmvPoint.setValue(gmvByDateMap.getOrDefault(day, BigDecimal.ZERO));
            gmvTrend.add(gmvPoint);
        }
        vo.setOrderTrend(orderTrend);
        vo.setGmvTrend(gmvTrend);

        List<OrderStatusStatVO> statusStats = new java.util.ArrayList<>();
        for (int status = 0; status <= 6; status++) {
            OrderStatusStatVO stat = new OrderStatusStatVO();
            stat.setStatus(status);
            stat.setCount(orderMapper.selectCount(
                new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderStatus, status)));
            statusStats.add(stat);
        }
        vo.setOrderStatusStats(statusStats);

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

    public PageResult<AdminOrderVO> getOrders(AdminOrderQueryDTO dto) {
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getOrderNo())) {
            wrapper.like(OrderEntity::getOrderNo, dto.getOrderNo().trim());
        }
        if (dto.getOrderStatus() != null) {
            wrapper.eq(OrderEntity::getOrderStatus, dto.getOrderStatus());
        }
        if (dto.getUserId() != null) {
            wrapper.eq(OrderEntity::getUserId, dto.getUserId());
        }
        if (dto.getShopId() != null) {
            wrapper.eq(OrderEntity::getShopId, dto.getShopId());
        }
        wrapper.orderByDesc(OrderEntity::getCreateTime);

        Page<OrderEntity> page = orderMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        if (page.getRecords().isEmpty()) {
            return PageResult.of(List.of(), page.getTotal(), dto.getPageNum(), dto.getPageSize());
        }

        Set<Long> shopIds = page.getRecords().stream().map(OrderEntity::getShopId).collect(Collectors.toSet());
        Set<Long> userIds = page.getRecords().stream().map(OrderEntity::getUserId).collect(Collectors.toSet());
        Map<Long, String> shopNameMap = shopMapper.selectBatchIds(shopIds).stream()
            .collect(Collectors.toMap(MerchantShopEntity::getId, MerchantShopEntity::getShopName));
        Map<Long, String> userNameMap = userMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));

        List<AdminOrderVO> list = page.getRecords().stream().map(order -> {
            AdminOrderVO vo = new AdminOrderVO();
            vo.setOrderNo(order.getOrderNo());
            vo.setUserId(order.getUserId());
            vo.setUsername(userNameMap.getOrDefault(order.getUserId(), "-"));
            vo.setShopId(order.getShopId());
            vo.setShopName(shopNameMap.getOrDefault(order.getShopId(), "-"));
            vo.setOrderStatus(order.getOrderStatus());
            vo.setPayStatus(order.getPayStatus());
            vo.setPayAmount(order.getPayAmount());
            vo.setCancelReason(order.getCancelReason());
            vo.setCreateTime(order.getCreateTime());
            vo.setPayTime(order.getPayTime());
            return vo;
        }).toList();

        return PageResult.of(list, page.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    public void cancelUnpaidOrder(String orderNo, String reason) {
        orderService.adminCancelUnpaidOrder(orderNo, reason, SecurityUtil.getCurrentUserId());
    }

    public void approveRefundByAdmin(String orderNo) {
        orderService.adminApproveRefund(orderNo, SecurityUtil.getCurrentUserId());
    }

    public void rejectRefundByAdmin(String orderNo, String reason) {
        orderService.adminRejectRefund(orderNo, reason, SecurityUtil.getCurrentUserId());
    }

    public SecurityOverviewVO getSecurityOverview() {
        SecurityOverviewVO vo = new SecurityOverviewVO();
        vo.setMaxFailures(loginSecurityService.getMaxFailures());
        vo.setLockMinutes(loginSecurityService.getLockMinutes());
        vo.setLockedAccountCount(loginSecurityService.getLockedAccountCount());
        vo.setTodayFailedLoginCount(loginSecurityService.getTodayFailedLoginCount());
        vo.setLockedAccounts(loginSecurityService.getLockedAccounts());
        return vo;
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
