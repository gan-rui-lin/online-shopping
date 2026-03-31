package com.helloworld.onlineshopping.modules.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.common.utils.OrderNoGenerator;
import com.helloworld.onlineshopping.modules.address.entity.UserAddressEntity;
import com.helloworld.onlineshopping.modules.address.mapper.UserAddressMapper;
import com.helloworld.onlineshopping.modules.cart.entity.CartItemEntity;
import com.helloworld.onlineshopping.modules.cart.mapper.CartItemMapper;
import com.helloworld.onlineshopping.modules.cart.service.CartService;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.order.dto.OrderQueryDTO;
import com.helloworld.onlineshopping.modules.order.dto.OrderSubmitDTO;
import com.helloworld.onlineshopping.modules.order.entity.*;
import com.helloworld.onlineshopping.modules.order.mapper.*;
import com.helloworld.onlineshopping.modules.order.mq.OrderMessageProducer;
import com.helloworld.onlineshopping.modules.order.vo.*;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderOperateLogMapper logMapper;
    private final PaymentRecordMapper paymentMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductSpuMapper spuMapper;
    private final UserAddressMapper addressMapper;
    private final MerchantShopMapper shopMapper;
    private final CartService cartService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderMessageProducer orderMessageProducer;
    private final OrderAsyncService orderAsyncService;

    private static final String ORDER_TIMEOUT_KEY = "order:timeout:zset";

    @Transactional
    public OrderSubmitVO submitOrder(OrderSubmitDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 1. Validate address
        UserAddressEntity address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("Invalid address");
        }

        // 2. Get cart items
        List<CartItemEntity> cartItems = cartItemMapper.selectList(
            new LambdaQueryWrapper<CartItemEntity>()
                .eq(CartItemEntity::getUserId, userId)
                .in(CartItemEntity::getSkuId, dto.getCartSkuIds()));
        if (cartItems.isEmpty()) {
            throw new BusinessException("Cart items not found");
        }

        // 3. Group by shop
        Map<Long, List<CartItemEntity>> shopGroups = new HashMap<>();
        for (CartItemEntity item : cartItems) {
            ProductSkuEntity sku = skuMapper.selectById(item.getSkuId());
            if (sku == null) throw new BusinessException("SKU not found: " + item.getSkuId());
            ProductSpuEntity spu = spuMapper.selectById(sku.getSpuId());
            if (spu == null) throw new BusinessException("Product not found");
            shopGroups.computeIfAbsent(spu.getShopId(), k -> new ArrayList<>()).add(item);
        }

        // 4. Create orders per shop
        List<String> orderNos = new ArrayList<>();
        OrderSubmitVO lastResult = null;

        for (Map.Entry<Long, List<CartItemEntity>> entry : shopGroups.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItemEntity> items = entry.getValue();

            String orderNo = OrderNoGenerator.generate();
            BigDecimal totalAmount = BigDecimal.ZERO;

            // Create order items and lock stock
            List<OrderItemEntity> orderItems = new ArrayList<>();
            for (CartItemEntity cartItem : items) {
                ProductSkuEntity sku = skuMapper.selectById(cartItem.getSkuId());
                ProductSpuEntity spu = spuMapper.selectById(sku.getSpuId());

                // Lock stock with optimistic locking
                int rows = skuMapper.lockStock(sku.getId(), cartItem.getQuantity(), sku.getVersion());
                if (rows == 0) {
                    throw new BusinessException("Insufficient stock for: " + spu.getTitle());
                }

                // Log inventory change
                InventoryLogEntity invLog = new InventoryLogEntity();
                invLog.setSkuId(sku.getId());
                invLog.setOrderNo(orderNo);
                invLog.setChangeCount(-cartItem.getQuantity());
                invLog.setBeforeStock(sku.getStock());
                invLog.setAfterStock(sku.getStock() - cartItem.getQuantity());
                invLog.setOperateType("LOCK");
                invLog.setRemark("Order submit lock stock");
                inventoryLogMapper.insert(invLog);

                BigDecimal itemTotal = sku.getSalePrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);

                OrderItemEntity orderItem = new OrderItemEntity();
                orderItem.setOrderNo(orderNo);
                orderItem.setSpuId(spu.getId());
                orderItem.setSkuId(sku.getId());
                orderItem.setProductTitle(spu.getTitle());
                orderItem.setSkuName(sku.getSkuName());
                orderItem.setSkuSpecJson(sku.getSpecJson());
                orderItem.setProductImage(sku.getImageUrl() != null ? sku.getImageUrl() : spu.getMainImage());
                orderItem.setSalePrice(sku.getSalePrice());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setTotalAmount(itemTotal);
                orderItem.setReviewStatus(0);
                orderItems.add(orderItem);
            }

            // Create order
            String fullAddress = address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress();
            OrderEntity order = new OrderEntity();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setShopId(shopId);
            order.setTotalAmount(totalAmount);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setPayAmount(totalAmount);
            order.setFreightAmount(BigDecimal.ZERO);
            order.setOrderStatus(0); // unpaid
            order.setPayStatus(0);
            order.setSourceType(1);
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());
            order.setReceiverAddress(fullAddress);
            order.setRemark(dto.getRemark());
            orderMapper.insert(order);

            // Set order id on items and insert
            for (OrderItemEntity oi : orderItems) {
                oi.setOrderId(order.getId());
                orderItemMapper.insert(oi);
            }

            // Log operation
            saveOperateLog(order.getId(), orderNo, null, 0, userId, "BUYER", "CREATE", "Order created");

            // Add to timeout queue (30 minutes)
            long expireTime = System.currentTimeMillis() + 30 * 60 * 1000;
            redisTemplate.opsForZSet().add(ORDER_TIMEOUT_KEY, orderNo, expireTime);
            try {
                orderMessageProducer.sendOrderTimeoutMessage(orderNo);
            } catch (Exception ex) {
                log.warn("Send order timeout message failed, order created without MQ", ex);
            }

            orderNos.add(orderNo);

            lastResult = new OrderSubmitVO();
            lastResult.setOrderNo(orderNo);
            lastResult.setTotalAmount(totalAmount);
            lastResult.setDiscountAmount(BigDecimal.ZERO);
            lastResult.setFreightAmount(BigDecimal.ZERO);
            lastResult.setPayAmount(totalAmount);
        }

        // 5. Clear purchased items from cart
        cartService.removeCheckedItems(userId, dto.getCartSkuIds());

        return lastResult;
    }

    @Transactional
    public PaymentVO payOrder(String orderNo) {
        Long userId = SecurityUtil.getCurrentUserId();
        OrderEntity order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("No permission");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("Order is not in unpaid status");
        }

        // Simulate payment
        String payNo = "PAY" + OrderNoGenerator.generate();
        LocalDateTime now = LocalDateTime.now();

        // Update order
        order.setOrderStatus(1); // to_ship
        order.setPayStatus(1);
        order.setPayTime(now);
        orderMapper.updateById(order);

        // Deduct locked stock
        List<OrderItemEntity> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemEntity>().eq(OrderItemEntity::getOrderId, order.getId()));
        for (OrderItemEntity item : items) {
            skuMapper.deductStock(item.getSkuId(), item.getQuantity());
            // Update SPU sales count
            ProductSpuEntity spu = spuMapper.selectById(item.getSpuId());
            if (spu != null) {
                spu.setSalesCount(spu.getSalesCount() + item.getQuantity());
                spuMapper.updateById(spu);
            }
        }

        // Create payment record
        PaymentRecordEntity payment = new PaymentRecordEntity();
        payment.setOrderNo(orderNo);
        payment.setPayNo(payNo);
        payment.setUserId(userId);
        payment.setPayAmount(order.getPayAmount());
        payment.setPayMethod(1);
        payment.setPayStatus(1);
        payment.setPayTime(now);
        paymentMapper.insert(payment);

        // Remove from timeout queue
        redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, orderNo);

        // Log
        saveOperateLog(order.getId(), orderNo, 0, 1, userId, "BUYER", "PAY", "Payment completed");
        for (OrderItemEntity item : items) {
            orderAsyncService.updateSalesStat(item.getSpuId(), item.getQuantity());
        }
        orderAsyncService.recordOrderStatusLog(orderNo, "PAY", "BUYER");

        PaymentVO vo = new PaymentVO();
        vo.setOrderNo(orderNo);
        vo.setPayNo(payNo);
        vo.setPayStatus(1);
        vo.setAmount(order.getPayAmount());
        vo.setPayTime(now);
        return vo;
    }

    @Transactional
    public void cancelOrder(String orderNo, String reason) {
        Long userId = SecurityUtil.getCurrentUserId();
        OrderEntity order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("No permission");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("Only unpaid orders can be cancelled");
        }

        doCancelOrder(order, reason, userId, "BUYER");
    }

    @Transactional
    public void doCancelOrder(OrderEntity order, String reason, Long operatorId, String role) {
        int beforeStatus = order.getOrderStatus();
        order.setOrderStatus(4);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        orderMapper.updateById(order);

        // Unlock stock
        List<OrderItemEntity> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemEntity>().eq(OrderItemEntity::getOrderId, order.getId()));
        for (OrderItemEntity item : items) {
            skuMapper.unlockStock(item.getSkuId(), item.getQuantity());

            ProductSkuEntity sku = skuMapper.selectById(item.getSkuId());
            InventoryLogEntity invLog = new InventoryLogEntity();
            invLog.setSkuId(item.getSkuId());
            invLog.setOrderNo(order.getOrderNo());
            invLog.setChangeCount(item.getQuantity());
            invLog.setBeforeStock(sku != null ? sku.getStock() : 0);
            invLog.setAfterStock(sku != null ? sku.getStock() + item.getQuantity() : item.getQuantity());
            invLog.setOperateType("UNLOCK");
            invLog.setRemark("Order cancelled, unlock stock");
            inventoryLogMapper.insert(invLog);
        }

        // Remove from timeout queue
        redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, order.getOrderNo());

        saveOperateLog(order.getId(), order.getOrderNo(), beforeStatus, 4, operatorId, role, "CANCEL", reason);
        orderAsyncService.recordOrderStatusLog(order.getOrderNo(), "CANCEL", role);
    }

    @Transactional
    public void cancelTimeoutOrderByOrderNo(String orderNo) {
        OrderEntity order = orderMapper.selectOne(
            new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderNo, orderNo));
        if (order == null) {
            redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, orderNo);
            return;
        }
        if (order.getOrderStatus() != 0) {
            redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, orderNo);
            return;
        }
        doCancelOrder(order, "Timeout auto cancel", 0L, "SYSTEM");
    }

    @Transactional
    public void confirmReceive(String orderNo) {
        Long userId = SecurityUtil.getCurrentUserId();
        OrderEntity order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("No permission");
        }
        if (order.getOrderStatus() != 2) {
            throw new BusinessException("Order is not in to_receive status");
        }

        order.setOrderStatus(3);
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(order);

        saveOperateLog(order.getId(), orderNo, 2, 3, userId, "BUYER", "CONFIRM_RECEIVE", "Buyer confirmed receive");
    }

    @Transactional
    public void deliverOrder(String orderNo) {
        Long userId = SecurityUtil.getCurrentUserId();
        OrderEntity order = getOrderByNo(orderNo);
        // Verify merchant owns this order's shop
        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null || !shop.getId().equals(order.getShopId())) {
            throw new BusinessException("No permission to deliver this order");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException("Order is not in to_ship status");
        }

        order.setOrderStatus(2);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.updateById(order);

        saveOperateLog(order.getId(), orderNo, 1, 2, userId, "MERCHANT", "DELIVER", "Merchant shipped order");
    }

    public OrderDetailVO getOrderDetail(String orderNo) {
        Long userId = SecurityUtil.getCurrentUserId();
        OrderEntity order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("No permission");
        }
        return buildOrderDetail(order);
    }

    public PageResult<OrderListVO> getOrderList(OrderQueryDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<OrderEntity> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<OrderEntity>()
            .eq(OrderEntity::getUserId, userId)
            .orderByDesc(OrderEntity::getCreateTime);
        if (dto.getOrderStatus() != null) {
            wrapper.eq(OrderEntity::getOrderStatus, dto.getOrderStatus());
        }

        Page<OrderEntity> result = orderMapper.selectPage(page, wrapper);
        List<OrderListVO> voList = result.getRecords().stream().map(order -> {
            OrderListVO vo = new OrderListVO();
            vo.setOrderNo(order.getOrderNo());
            vo.setShopId(order.getShopId());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setPayAmount(order.getPayAmount());
            vo.setCreateTime(order.getCreateTime());

            MerchantShopEntity shop = shopMapper.selectById(order.getShopId());
            vo.setShopName(shop != null ? shop.getShopName() : "");

            List<OrderItemEntity> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemEntity>().eq(OrderItemEntity::getOrderId, order.getId()));
            vo.setItemList(items.stream().map(this::toItemVO).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    private OrderEntity getOrderByNo(String orderNo) {
        OrderEntity order = orderMapper.selectOne(
            new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        return order;
    }

    private OrderDetailVO buildOrderDetail(OrderEntity order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setDeliveryTime(order.getDeliveryTime());
        vo.setFinishTime(order.getFinishTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCancelReason(order.getCancelReason());

        List<OrderItemEntity> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemEntity>().eq(OrderItemEntity::getOrderId, order.getId()));
        vo.setItemList(items.stream().map(this::toItemVO).collect(Collectors.toList()));
        return vo;
    }

    private OrderItemVO toItemVO(OrderItemEntity entity) {
        OrderItemVO vo = new OrderItemVO();
        vo.setSpuId(entity.getSpuId());
        vo.setSkuId(entity.getSkuId());
        vo.setProductTitle(entity.getProductTitle());
        vo.setSkuName(entity.getSkuName());
        vo.setSkuSpecJson(entity.getSkuSpecJson());
        vo.setProductImage(entity.getProductImage());
        vo.setSalePrice(entity.getSalePrice());
        vo.setQuantity(entity.getQuantity());
        vo.setTotalAmount(entity.getTotalAmount());
        vo.setReviewStatus(entity.getReviewStatus());
        return vo;
    }

    private void saveOperateLog(Long orderId, String orderNo, Integer before, Integer after,
                                Long operatorId, String role, String type, String remark) {
        OrderOperateLogEntity log = new OrderOperateLogEntity();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setBeforeStatus(before);
        log.setAfterStatus(after);
        log.setOperatorId(operatorId);
        log.setOperatorRole(role);
        log.setOperateType(type);
        log.setRemark(remark);
        log.setOperateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    public PageResult<OrderListVO> getMerchantOrders(OrderQueryDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null) {
            throw new BusinessException("You don't have a shop yet");
        }

        Page<OrderEntity> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<OrderEntity>()
            .eq(OrderEntity::getShopId, shop.getId())
            .orderByDesc(OrderEntity::getCreateTime);
        if (dto.getOrderStatus() != null) {
            wrapper.eq(OrderEntity::getOrderStatus, dto.getOrderStatus());
        }

        Page<OrderEntity> result = orderMapper.selectPage(page, wrapper);
        List<OrderListVO> voList = result.getRecords().stream().map(order -> {
            OrderListVO vo = new OrderListVO();
            vo.setOrderNo(order.getOrderNo());
            vo.setShopId(order.getShopId());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setPayAmount(order.getPayAmount());
            vo.setCreateTime(order.getCreateTime());
            vo.setShopName(shop.getShopName());

            List<OrderItemEntity> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemEntity>().eq(OrderItemEntity::getOrderId, order.getId()));
            vo.setItemList(items.stream().map(this::toItemVO).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    @Transactional
    public void applyRefund(String orderNo, String reason) {
        Long userId = SecurityUtil.getCurrentUserId();
        OrderEntity order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("No permission");
        }
        if (order.getOrderStatus() != 2 && order.getOrderStatus() != 3) {
            throw new BusinessException("Only received or completed orders can request refund");
        }

        int beforeStatus = order.getOrderStatus();
        order.setOrderStatus(5); // refunding
        orderMapper.updateById(order);

        saveOperateLog(order.getId(), orderNo, beforeStatus, 5, userId, "BUYER", "APPLY_REFUND", reason);
    }

    @Transactional
    public void approveRefund(String orderNo) {
        Long userId = SecurityUtil.getCurrentUserId();
        OrderEntity order = getOrderByNo(orderNo);

        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null || !shop.getId().equals(order.getShopId())) {
            throw new BusinessException("No permission");
        }
        if (order.getOrderStatus() != 5) {
            throw new BusinessException("Order is not in refunding status");
        }

        // Process refund
        order.setOrderStatus(6); // refunded
        order.setPayStatus(2); // refunded
        orderMapper.updateById(order);

        // Return stock
        List<OrderItemEntity> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemEntity>().eq(OrderItemEntity::getOrderId, order.getId()));
        for (OrderItemEntity item : items) {
            ProductSkuEntity sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                sku.setStock(sku.getStock() + item.getQuantity());
                skuMapper.updateById(sku);

                InventoryLogEntity invLog = new InventoryLogEntity();
                invLog.setSkuId(item.getSkuId());
                invLog.setOrderNo(orderNo);
                invLog.setChangeCount(item.getQuantity());
                invLog.setBeforeStock(sku.getStock() - item.getQuantity());
                invLog.setAfterStock(sku.getStock());
                invLog.setOperateType("RETURN");
                invLog.setRemark("Refund approved, return stock");
                inventoryLogMapper.insert(invLog);
            }

            // Decrease SPU sales count
            ProductSpuEntity spu = spuMapper.selectById(item.getSpuId());
            if (spu != null) {
                spu.setSalesCount(Math.max(0, spu.getSalesCount() - item.getQuantity()));
                spuMapper.updateById(spu);
            }
        }

        saveOperateLog(order.getId(), orderNo, 5, 6, userId, "MERCHANT", "APPROVE_REFUND", "Merchant approved refund");
    }

    @Transactional
    public void rejectRefund(String orderNo, String reason) {
        Long userId = SecurityUtil.getCurrentUserId();
        OrderEntity order = getOrderByNo(orderNo);

        MerchantShopEntity shop = shopMapper.selectOne(
            new LambdaQueryWrapper<MerchantShopEntity>().eq(MerchantShopEntity::getUserId, userId));
        if (shop == null || !shop.getId().equals(order.getShopId())) {
            throw new BusinessException("No permission");
        }
        if (order.getOrderStatus() != 5) {
            throw new BusinessException("Order is not in refunding status");
        }

        order.setOrderStatus(3); // back to completed
        orderMapper.updateById(order);

        saveOperateLog(order.getId(), orderNo, 5, 3, userId, "MERCHANT", "REJECT_REFUND", reason);
    }

    public com.helloworld.onlineshopping.modules.order.vo.DeliveryDetailVO getDeliveryDetails(String orderNo) {
        Long userId = SecurityUtil.getCurrentUserId();
        OrderEntity order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("No permission");
        }

        com.helloworld.onlineshopping.modules.order.vo.DeliveryDetailVO vo = new com.helloworld.onlineshopping.modules.order.vo.DeliveryDetailVO();
        vo.setOrderNo(orderNo);
        vo.setStatus(order.getOrderStatus());
        vo.setDeliveryTime(order.getDeliveryTime());

        // Simulated tracking info
        if (order.getOrderStatus() >= 2) {
            vo.setTrackingNo("SF" + orderNo.substring(orderNo.length() - 10));
            vo.setCarrier("SF Express");
            vo.setCurrentLocation("In transit");
            if (order.getDeliveryTime() != null) {
                vo.setEstimatedTime(order.getDeliveryTime().plusDays(2));
            }
        }

        return vo;
    }
}
