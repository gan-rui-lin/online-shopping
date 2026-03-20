package com.helloworld.onlineshopping.modules.order;

import com.helloworld.onlineshopping.common.security.LoginUser;
import com.helloworld.onlineshopping.modules.address.entity.UserAddressEntity;
import com.helloworld.onlineshopping.modules.address.mapper.UserAddressMapper;
import com.helloworld.onlineshopping.modules.cart.entity.CartItemEntity;
import com.helloworld.onlineshopping.modules.cart.mapper.CartItemMapper;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.order.dto.OrderSubmitDTO;
import com.helloworld.onlineshopping.modules.order.entity.OrderEntity;
import com.helloworld.onlineshopping.modules.order.entity.OrderItemEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderItemMapper;
import com.helloworld.onlineshopping.modules.order.mapper.OrderMapper;
import com.helloworld.onlineshopping.modules.order.service.OrderService;
import com.helloworld.onlineshopping.modules.order.vo.OrderSubmitVO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private UserAddressMapper addressMapper;
    @Autowired private CartItemMapper cartItemMapper;
    @Autowired private ProductSpuMapper spuMapper;
    @Autowired private ProductSkuMapper skuMapper;
    @Autowired private MerchantShopMapper shopMapper;

    private Long userId;
    private Long skuId;
    private Long addressId;

    @BeforeEach
    void setUp() {
        userId = 10001L;
        LoginUser loginUser = new LoginUser(userId, "ordertest", List.of("ROLE_BUYER"));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null,
                List.of(new SimpleGrantedAuthority("ROLE_BUYER"))));

        MerchantShopEntity shop = new MerchantShopEntity();
        shop.setUserId(20001L);
        shop.setShopName("Order Test Shop");
        shop.setShopStatus(1);
        shop.setScore(new BigDecimal("4.8"));
        shopMapper.insert(shop);

        ProductSpuEntity spu = new ProductSpuEntity();
        spu.setShopId(shop.getId());
        spu.setCategoryId(1L);
        spu.setTitle("Order Test Product");
        spu.setStatus(1);
        spu.setAuditStatus(1);
        spu.setMinPrice(new BigDecimal("59.00"));
        spu.setMaxPrice(new BigDecimal("59.00"));
        spu.setSalesCount(0);
        spu.setLikeCount(0);
        spu.setFavoriteCount(0);
        spu.setBrowseCount(0);
        spuMapper.insert(spu);

        ProductSkuEntity sku = new ProductSkuEntity();
        sku.setSpuId(spu.getId());
        sku.setSkuCode("ORDER-SKU-001");
        sku.setSkuName("Order SKU");
        sku.setSalePrice(new BigDecimal("59.00"));
        sku.setStock(20);
        sku.setLockStock(0);
        sku.setStatus(1);
        sku.setVersion(0);
        skuMapper.insert(sku);
        skuId = sku.getId();

        UserAddressEntity address = new UserAddressEntity();
        address.setUserId(userId);
        address.setReceiverName("Test User");
        address.setReceiverPhone("13800000000");
        address.setProvince("ZJ");
        address.setCity("HZ");
        address.setDistrict("XC");
        address.setDetailAddress("No.1 Road");
        address.setIsDefault(1);
        addressMapper.insert(address);
        addressId = address.getId();

        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setUserId(userId);
        cartItem.setSkuId(skuId);
        cartItem.setQuantity(2);
        cartItem.setChecked(1);
        cartItemMapper.insert(cartItem);
    }

    @Test
    void testSubmitOrderCreatesItems() {
        OrderSubmitDTO dto = new OrderSubmitDTO();
        dto.setAddressId(addressId);
        dto.setCartSkuIds(List.of(skuId));
        dto.setRemark("test batch");

        OrderSubmitVO result = orderService.submitOrder(dto);
        assertNotNull(result.getOrderNo());

        OrderEntity order = orderMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, result.getOrderNo()));
        assertNotNull(order);

        List<OrderItemEntity> items = orderItemMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItemEntity>()
                .eq(OrderItemEntity::getOrderId, order.getId()));
        assertEquals(1, items.size());

        ProductSkuEntity sku = skuMapper.selectById(skuId);
        assertEquals(2, sku.getLockStock());
    }
}
