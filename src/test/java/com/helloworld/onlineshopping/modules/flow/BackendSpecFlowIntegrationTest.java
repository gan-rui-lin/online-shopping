package com.helloworld.onlineshopping.modules.flow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helloworld.onlineshopping.modules.order.entity.OrderItemEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderItemMapper;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BackendSpecFlowIntegrationTest {

    private static final String DEFAULT_TEST_PASSWORD =
        System.getProperty("test.password", "test123456");
    private static final String ADMIN_USERNAME =
        System.getProperty("test.admin.username", "admin");
    private static final String ADMIN_PASSWORD =
        System.getProperty("test.admin.password", "admin123");
    private static final Long DEFAULT_CATEGORY_ID =
        Long.parseLong(System.getProperty("test.default.categoryId", "1"));
    private static final String DEFAULT_CONTACT_PHONE =
        System.getProperty("test.contact.phone", "13800138000");
    private static final String DEFAULT_RECEIVER_PHONE =
        System.getProperty("test.receiver.phone", "13900000000");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductSpuMapper productSpuMapper;

    @Autowired
    private ProductSkuMapper productSkuMapper;

    @Autowired
    private MerchantShopMapper merchantShopMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    /**
     * 说明：覆盖规格说明书中的核心主流程，从认证到订单闭环再到评价。
     * 样例：buyerA 下单商品“SpecFlow Phone”，merchantA 发货后 buyerA 确认收货并评价。
     */
    @Test
    @DisplayName("规格主流程集成测试：注册登录-商家入驻-商品上架-下单支付-发货收货-评价")
    void shouldCompleteCoreBusinessFlowFromSpec() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String buyerUsername = "buyer_" + suffix;
        String merchantUsername = "merchant_" + suffix;
        String password = DEFAULT_TEST_PASSWORD;

        register(buyerUsername, password, "Buyer " + suffix);
        register(merchantUsername, password, "Merchant " + suffix);

        String buyerToken = loginAndGetToken(buyerUsername, password);
        String merchantToken = loginAndGetToken(merchantUsername, password);
        String adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);

        String shopName = "Shop-" + suffix;
        mockMvc.perform(post("/api/merchant/apply")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "shopName":"%s",
                      "businessLicenseNo":"BL-%s",
                      "contactName":"Owner %s",
                                            "contactPhone":"%s"
                    }
                                        """.formatted(shopName, suffix, suffix, DEFAULT_CONTACT_PHONE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        MvcResult applyListResult = mockMvc.perform(get("/api/merchant/apply/list")
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        JsonNode applyListJson = parseBody(applyListResult);
        Long applyId = null;
        for (JsonNode node : applyListJson.path("data")) {
            if (shopName.equals(node.path("shopName").asText())) {
                applyId = node.path("id").asLong();
                break;
            }
        }
        assertNotNull(applyId);

        mockMvc.perform(post("/api/merchant/apply/{id}/audit", applyId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "auditStatus":1,
                      "remark":"approved by integration test"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        merchantToken = loginAndGetToken(merchantUsername, password);

        String productTitle = "SpecFlow Phone " + suffix;
        mockMvc.perform(post("/api/products")
                .header("Authorization", bearer(merchantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "categoryId":%d,
                      "brandName":"SpecBrand",
                      "title":"%s",
                      "subTitle":"Spec Flow Device",
                      "mainImage":"https://example.com/specflow.png",
                      "detailText":"integration test product",
                      "skuList":[
                        {
                          "skuCode":"SKU-%s",
                          "skuName":"Default",
                          "specJson":"{\\"color\\":\\"black\\"}",
                          "price":999.00,
                          "originPrice":1299.00,
                          "stock":50,
                          "imageUrl":"https://example.com/specflow-sku.png"
                        }
                      ],
                      "imageList":["https://example.com/specflow-1.png"]
                    }
                    """.formatted(DEFAULT_CATEGORY_ID, productTitle, suffix)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        ProductSpuEntity spu = productSpuMapper.selectOne(
            new LambdaQueryWrapper<ProductSpuEntity>().eq(ProductSpuEntity::getTitle, productTitle));
        assertNotNull(spu);
        Long spuId = spu.getId();

        mockMvc.perform(put("/api/products/{spuId}/on-shelf", spuId)
                .header("Authorization", bearer(merchantToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("Only approved products can be put on shelf"));

        mockMvc.perform(post("/api/admin/product/{spuId}/approve", spuId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(put("/api/products/{spuId}/on-shelf", spuId)
                .header("Authorization", bearer(merchantToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/products")
                .param("keyword", "SpecFlow")
                .param("pageNum", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/products/{spuId}", spuId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.spuId").value(spuId));

        mockMvc.perform(post("/api/address")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "receiverName":"Buyer Receiver",
                      "receiverPhone":"%s",
                      "province":"广东省",
                      "city":"深圳市",
                      "district":"南山区",
                      "detailAddress":"科技园 1 号",
                      "isDefault":1,
                      "tagName":"home"
                    }
                    """.formatted(DEFAULT_RECEIVER_PHONE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        MvcResult addressListResult = mockMvc.perform(get("/api/address/list")
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        JsonNode addressListJson = parseBody(addressListResult);
        Long addressId = addressListJson.path("data").get(0).path("id").asLong();

        ProductSkuEntity sku = productSkuMapper.selectOne(
            new LambdaQueryWrapper<ProductSkuEntity>().eq(ProductSkuEntity::getSpuId, spuId));
        assertNotNull(sku);
        Long skuId = sku.getId();

        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "skuId":%d,
                      "quantity":1
                    }
                    """.formatted(skuId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        MvcResult submitOrderResult = mockMvc.perform(post("/api/order/submit")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "addressId":%d,
                      "remark":"spec flow order",
                      "cartSkuIds":[%d]
                    }
                    """.formatted(addressId, skuId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        String orderNo = parseBody(submitOrderResult).path("data").path("orderNo").asText();

        mockMvc.perform(post("/api/order/{orderNo}/pay", orderNo)
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/order/{orderNo}/deliver", orderNo)
                .header("Authorization", bearer(merchantToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/order/{orderNo}/confirm-receive", orderNo)
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        OrderItemEntity orderItem = orderItemMapper.selectOne(
            new LambdaQueryWrapper<OrderItemEntity>()
                .eq(OrderItemEntity::getOrderNo, orderNo)
                .eq(OrderItemEntity::getSkuId, skuId));
        assertNotNull(orderItem);

        mockMvc.perform(post("/api/review")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "orderItemId":%d,
                      "score":5,
                      "content":"Great product from integration flow",
                      "anonymousFlag":0
                    }
                    """.formatted(orderItem.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        MvcResult reviewListResult = mockMvc.perform(get("/api/review/product/{spuId}", spuId)
                .param("pageNum", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        Long reviewId = parseBody(reviewListResult).path("data").path("list").get(0).path("reviewId").asLong();

        mockMvc.perform(post("/api/review/{reviewId}/reply", reviewId)
                .header("Authorization", bearer(merchantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "replyContent":"Thanks for your feedback"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/review/product/{spuId}/statistics", spuId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 说明：覆盖规格中的订单取消分支，验证“未支付订单可取消”。
     * 样例：buyerB 提交订单后立即取消，接口返回成功。
     */
    @Test
    @DisplayName("规格分支流程测试：未支付订单取消")
    void shouldCancelUnpaidOrder() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "buyer_cancel_" + suffix;
        String password = DEFAULT_TEST_PASSWORD;

        register(username, password, "BuyerCancel " + suffix);
        String token = loginAndGetToken(username, password);

        MvcResult createAddressResult = mockMvc.perform(post("/api/address")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "receiverName":"Cancel Receiver",
                      "receiverPhone":"13911111111",
                      "province":"广东省",
                      "city":"广州市",
                      "district":"天河区",
                      "detailAddress":"测试路 2 号",
                      "isDefault":1,
                      "tagName":"home"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        assertNotNull(createAddressResult);

        MerchantShopEntity shop = new MerchantShopEntity();
        shop.setUserId(Math.abs((long) suffix.hashCode()) + 10000L);
        shop.setShopName("CancelFlowShop-" + suffix);
        shop.setShopStatus(1);
        shop.setScore(new BigDecimal("5.0"));
        merchantShopMapper.insert(shop);

        ProductSpuEntity spu = new ProductSpuEntity();
        spu.setShopId(shop.getId());
        spu.setCategoryId(DEFAULT_CATEGORY_ID);
        spu.setBrandName("CancelBrand");
        spu.setTitle("Cancel Flow Product " + suffix);
        spu.setSubTitle("cancel test spu");
        spu.setStatus(1);
        spu.setAuditStatus(1);
        spu.setMinPrice(new BigDecimal("99.00"));
        spu.setMaxPrice(new BigDecimal("99.00"));
        spu.setSalesCount(0);
        spu.setLikeCount(0);
        spu.setFavoriteCount(0);
        spu.setBrowseCount(0);
        productSpuMapper.insert(spu);

        ProductSkuEntity anySku = new ProductSkuEntity();
        anySku.setSpuId(spu.getId());
        anySku.setSkuCode("CANCEL-SKU-" + suffix);
        anySku.setSkuName("CancelSKU");
        anySku.setSalePrice(new BigDecimal("99.00"));
        anySku.setOriginPrice(new BigDecimal("129.00"));
        anySku.setStock(100);
        anySku.setLockStock(0);
        anySku.setWarningStock(10);
        anySku.setStatus(1);
        anySku.setVersion(0);
        productSkuMapper.insert(anySku);

        MvcResult addressListResult = mockMvc.perform(get("/api/address/list")
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        Long addressId = parseBody(addressListResult).path("data").get(0).path("id").asLong();

        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "skuId":%d,
                      "quantity":1
                    }
                    """.formatted(anySku.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        MvcResult submitOrderResult = mockMvc.perform(post("/api/order/submit")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "addressId":%d,
                      "remark":"cancel branch",
                      "cartSkuIds":[%d]
                    }
                    """.formatted(addressId, anySku.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        String orderNo = parseBody(submitOrderResult).path("data").path("orderNo").asText();

        mockMvc.perform(post("/api/order/{orderNo}/cancel", orderNo)
                .header("Authorization", bearer(token))
                .param("reason", "test cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("规格分支流程测试：退款申请-商家同意/拒绝")
    void shouldProcessRefundApproveAndReject() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String buyerUsername = "buyer_refund_" + suffix;
        String merchantUsername = "merchant_refund_" + suffix;
        String password = DEFAULT_TEST_PASSWORD;

        register(buyerUsername, password, "BuyerRefund " + suffix);
        register(merchantUsername, password, "MerchantRefund " + suffix);

        String buyerToken = loginAndGetToken(buyerUsername, password);
        String merchantToken = loginAndGetToken(merchantUsername, password);
        String adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);

        String shopName = "RefundShop-" + suffix;
        mockMvc.perform(post("/api/merchant/apply")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "shopName":"%s",
                      "businessLicenseNo":"BLR-%s",
                      "contactName":"Owner %s",
                      "contactPhone":"%s"
                    }
                    """.formatted(shopName, suffix, suffix, DEFAULT_CONTACT_PHONE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        MvcResult applyListResult = mockMvc.perform(get("/api/merchant/apply/list")
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        Long applyId = null;
        for (JsonNode node : parseBody(applyListResult).path("data")) {
            if (shopName.equals(node.path("shopName").asText())) {
                applyId = node.path("id").asLong();
                break;
            }
        }
        assertNotNull(applyId);

        mockMvc.perform(post("/api/merchant/apply/{id}/audit", applyId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "auditStatus":1,
                      "remark":"approved by refund test"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        merchantToken = loginAndGetToken(merchantUsername, password);

        String productTitle = "Refund Flow Product " + suffix;
        mockMvc.perform(post("/api/products")
                .header("Authorization", bearer(merchantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "categoryId":%d,
                      "brandName":"RefundBrand",
                      "title":"%s",
                      "subTitle":"Refund Device",
                      "mainImage":"https://example.com/refund.png",
                      "detailText":"refund test product",
                      "skuList":[
                        {
                          "skuCode":"RSKU-%s",
                          "skuName":"RefundSKU",
                          "specJson":"{\\"color\\":\\"white\\"}",
                          "price":299.00,
                          "originPrice":399.00,
                          "stock":20,
                          "imageUrl":"https://example.com/refund-sku.png"
                        }
                      ]
                    }
                    """.formatted(DEFAULT_CATEGORY_ID, productTitle, suffix)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        ProductSpuEntity spu = productSpuMapper.selectOne(
            new LambdaQueryWrapper<ProductSpuEntity>().eq(ProductSpuEntity::getTitle, productTitle));
        assertNotNull(spu);
        Long spuId = spu.getId();

        mockMvc.perform(post("/api/admin/product/{spuId}/approve", spuId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(put("/api/products/{spuId}/on-shelf", spuId)
                .header("Authorization", bearer(merchantToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/address")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "receiverName":"Refund Receiver",
                      "receiverPhone":"%s",
                      "province":"广东省",
                      "city":"深圳市",
                      "district":"南山区",
                      "detailAddress":"退款路 2 号",
                      "isDefault":1,
                      "tagName":"home"
                    }
                    """.formatted(DEFAULT_RECEIVER_PHONE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        MvcResult addressListResult = mockMvc.perform(get("/api/address/list")
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        Long addressId = parseBody(addressListResult).path("data").get(0).path("id").asLong();

        ProductSkuEntity sku = productSkuMapper.selectOne(
            new LambdaQueryWrapper<ProductSkuEntity>().eq(ProductSkuEntity::getSpuId, spuId));
        assertNotNull(sku);

        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "skuId":%d,
                      "quantity":1
                    }
                    """.formatted(sku.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        MvcResult submitOrderResult = mockMvc.perform(post("/api/order/submit")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "addressId":%d,
                      "remark":"refund approve flow",
                      "cartSkuIds":[%d]
                    }
                    """.formatted(addressId, sku.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        String orderNoApprove = parseBody(submitOrderResult).path("data").path("orderNo").asText();

        mockMvc.perform(post("/api/order/{orderNo}/pay", orderNoApprove)
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(post("/api/order/{orderNo}/deliver", orderNoApprove)
                .header("Authorization", bearer(merchantToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(post("/api/order/{orderNo}/confirm-receive", orderNoApprove)
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/order/{orderNo}/refund/apply", orderNoApprove)
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason":"need refund approve flow"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/order/{orderNo}/refund/approve", orderNoApprove)
                .header("Authorization", bearer(merchantToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/order/{orderNo}", orderNoApprove)
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.orderStatus").value(6))
            .andExpect(jsonPath("$.data.payStatus").value(2));

        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "skuId":%d,
                      "quantity":1
                    }
                    """.formatted(sku.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        MvcResult submitOrderResult2 = mockMvc.perform(post("/api/order/submit")
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "addressId":%d,
                      "remark":"refund reject flow",
                      "cartSkuIds":[%d]
                    }
                    """.formatted(addressId, sku.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
        String orderNoReject = parseBody(submitOrderResult2).path("data").path("orderNo").asText();

        mockMvc.perform(post("/api/order/{orderNo}/pay", orderNoReject)
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(post("/api/order/{orderNo}/deliver", orderNoReject)
                .header("Authorization", bearer(merchantToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(post("/api/order/{orderNo}/confirm-receive", orderNoReject)
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/order/{orderNo}/refund/apply", orderNoReject)
                .header("Authorization", bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason":"need refund reject flow"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/order/{orderNo}/refund/reject", orderNoReject)
                .header("Authorization", bearer(merchantToken))
                .param("reason", "not eligible"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/order/{orderNo}", orderNoReject)
                .header("Authorization", bearer(buyerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.orderStatus").value(3))
            .andExpect(jsonPath("$.data.payStatus").value(1));
    }

    private void register(String username, String password, String nickname) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username":"%s",
                      "password":"%s",
                      "nickname":"%s"
                    }
                    """.formatted(username, password, nickname)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username":"%s",
                      "password":"%s"
                    }
                    """.formatted(username, password)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        return parseBody(loginResult).path("data").path("token").asText();
    }

    private JsonNode parseBody(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(content);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
