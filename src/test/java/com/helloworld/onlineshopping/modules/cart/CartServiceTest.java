package com.helloworld.onlineshopping.modules.cart;

import com.helloworld.onlineshopping.common.security.LoginUser;
import com.helloworld.onlineshopping.modules.cart.dto.CartAddDTO;
import com.helloworld.onlineshopping.modules.cart.dto.CartUpdateDTO;
import com.helloworld.onlineshopping.modules.cart.service.CartService;
import com.helloworld.onlineshopping.modules.cart.vo.CartVO;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceTest {

    @Autowired private CartService cartService;
    @Autowired private ProductSpuMapper spuMapper;
    @Autowired private ProductSkuMapper skuMapper;
    @Autowired private MerchantShopMapper shopMapper;

    private Long testSkuId;

    @BeforeEach
    void setUp() {
        // Set security context
        LoginUser loginUser = new LoginUser(9999L, "carttest", List.of("ROLE_BUYER"));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null,
                List.of(new SimpleGrantedAuthority("ROLE_BUYER"))));

        // Create shop
        MerchantShopEntity shop = new MerchantShopEntity();
        shop.setUserId(9998L);
        shop.setShopName("Test Shop");
        shop.setShopStatus(1);
        shop.setScore(new BigDecimal("5.0"));
        shopMapper.insert(shop);

        // Create SPU
        ProductSpuEntity spu = new ProductSpuEntity();
        spu.setShopId(shop.getId());
        spu.setCategoryId(1L);
        spu.setTitle("Test Product");
        spu.setStatus(1);
        spu.setAuditStatus(1);
        spu.setMinPrice(new BigDecimal("99.00"));
        spu.setMaxPrice(new BigDecimal("99.00"));
        spu.setSalesCount(0);
        spu.setLikeCount(0);
        spu.setFavoriteCount(0);
        spu.setBrowseCount(0);
        spuMapper.insert(spu);

        // Create SKU
        ProductSkuEntity sku = new ProductSkuEntity();
        sku.setSpuId(spu.getId());
        sku.setSkuCode("TEST-SKU-001");
        sku.setSkuName("Test SKU");
        sku.setSalePrice(new BigDecimal("99.00"));
        sku.setStock(100);
        sku.setLockStock(0);
        sku.setStatus(1);
        sku.setVersion(0);
        skuMapper.insert(sku);
        testSkuId = sku.getId();
    }

    @Test
    void testAddItem() {
        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(testSkuId);
        dto.setQuantity(1);
        cartService.addItem(dto);
        CartVO cart = cartService.getCartList();
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void testAddItemDuplicate() {
        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(testSkuId);
        dto.setQuantity(1);
        cartService.addItem(dto);
        cartService.addItem(dto);
        CartVO cart = cartService.getCartList();
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void testUpdateItem() {
        CartAddDTO add = new CartAddDTO();
        add.setSkuId(testSkuId);
        add.setQuantity(1);
        cartService.addItem(add);

        CartUpdateDTO upd = new CartUpdateDTO();
        upd.setSkuId(testSkuId);
        upd.setQuantity(5);
        cartService.updateItem(upd);

        CartVO cart = cartService.getCartList();
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void testRemoveItem() {
        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(testSkuId);
        dto.setQuantity(1);
        cartService.addItem(dto);
        cartService.removeItem(testSkuId);
        CartVO cart = cartService.getCartList();
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void testBatchAddItems() {
        ProductSkuEntity baseSku = skuMapper.selectById(testSkuId);
        ProductSkuEntity secondSku = new ProductSkuEntity();
        secondSku.setSpuId(baseSku.getSpuId());
        secondSku.setSkuCode("TEST-SKU-002");
        secondSku.setSkuName("Test SKU 2");
        secondSku.setSalePrice(new BigDecimal("109.00"));
        secondSku.setStock(50);
        secondSku.setLockStock(0);
        secondSku.setStatus(1);
        secondSku.setVersion(0);
        skuMapper.insert(secondSku);

        CartAddDTO item1 = new CartAddDTO();
        item1.setSkuId(testSkuId);
        item1.setQuantity(2);

        CartAddDTO item2 = new CartAddDTO();
        item2.setSkuId(secondSku.getId());
        item2.setQuantity(1);

        List<CartAddDTO> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        cartService.addItemsBatch(items);

        CartVO cart = cartService.getCartList();
        assertEquals(2, cart.getItems().size());
    }
}
