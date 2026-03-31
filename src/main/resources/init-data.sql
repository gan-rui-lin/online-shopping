-- =============================================
-- Online Shopping Platform - Test Data
-- =============================================
-- Purpose:
-- 1) Clear all table data in the current schema.
-- 2) Seed only one test user: buyer.
-- 3) Provide realistic products with detailed descriptions.
-- 4) Add multiple browse history and favorites for buyer.
-- 5) No shopping plan data is inserted.
--
-- NOTE: This script is destructive for data in online_shopping.
-- mysql -h localhost -P 3306 -u root -p{your mysql password} online_shopping -e "source ./src/main/resources/init_data.sql"
-- =============================================

USE online_shopping;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Clear all business tables
TRUNCATE TABLE order_operate_log;
TRUNCATE TABLE payment_record;
TRUNCATE TABLE inventory_log;
TRUNCATE TABLE order_item;
TRUNCATE TABLE order_info;
TRUNCATE TABLE product_review;
TRUNCATE TABLE cart_item;
TRUNCATE TABLE user_address;
TRUNCATE TABLE user_favorite;
TRUNCATE TABLE user_browse_history;

TRUNCATE TABLE product_image;
TRUNCATE TABLE product_sku;
TRUNCATE TABLE product_spu;
TRUNCATE TABLE product_category;

TRUNCATE TABLE merchant_apply;
TRUNCATE TABLE merchant_shop;

TRUNCATE TABLE ai_chat_message;
TRUNCATE TABLE ai_chat_session;
TRUNCATE TABLE product_knowledge_doc;
TRUNCATE TABLE shopping_plan_item;
TRUNCATE TABLE shopping_plan;
TRUNCATE TABLE agent_task;

TRUNCATE TABLE sys_role_permission;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_permission;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_user;

SET FOREIGN_KEY_CHECKS = 1;

-- 2. Base roles
INSERT INTO sys_role (id, role_code, role_name, status) VALUES
(1, 'ROLE_BUYER', 'Buyer', 1),
(2, 'ROLE_MERCHANT', 'Merchant', 1),
(3, 'ROLE_ADMIN', 'Administrator', 1);

-- 3. Only one user: buyer
INSERT INTO sys_user (id, username, password, nickname, phone, user_type, status, email) VALUES
(7700, 'buyer', '$2a$10$P72FmkCB4SqX4mxchQDgH.IGPFtSwhpiVFdELxZlQhF8OUEMIlm8a', 'Buyer', '13900007777', 1, 1, 'buyer@test.local');

INSERT INTO sys_user_role (user_id, role_id) VALUES
(7700, 1);

-- 4. Categories (full baseline)
INSERT INTO product_category (id, parent_id, category_name, level, sort_order, status) VALUES
(1, 0, 'Electronics', 1, 1, 1),
(2, 0, 'Clothing', 1, 2, 1),
(3, 0, 'Home & Living', 1, 3, 1),
(4, 0, 'Books', 1, 4, 1),
(5, 0, 'Food & Beverage', 1, 5, 1),
(6, 1, 'Phones', 2, 1, 1),
(7, 1, 'Laptops', 2, 2, 1),
(8, 1, 'Accessories', 2, 3, 1),
(9, 2, 'Men', 2, 1, 1),
(10, 2, 'Women', 2, 2, 1);

-- 5. One active shop record (no additional user account created)
INSERT INTO merchant_shop (id, user_id, shop_name, shop_desc, shop_status, score) VALUES
(8800, 999999, 'Buyer Select Store', 'Official test storefront for product browsing and favorites scenarios.', 1, 4.90);

-- 6. Products (realistic and detailed descriptions)
INSERT INTO product_spu (
    id, shop_id, category_id, brand_name, title, sub_title, main_image, detail_text,
    status, audit_status, min_price, max_price, sales_count, browse_count, favorite_count, like_count
) VALUES
(9000, 8800, 6, 'Apple', 'iPhone 15 Pro Max', 'A17 Pro, titanium body, and professional mobile photography.', '/cdn/images/9d950947-710e-4e14-8092-7c880689d50c.jpg',
 'Highlights: 6.7-inch Super Retina XDR display, 120Hz ProMotion, A17 Pro chip, and USB-C interface. Camera system includes 48MP main sensor, 5x telephoto, and advanced Night mode. Battery supports all-day mixed usage and fast charging with 20W adapter. In-box content: iPhone, USB-C cable, and documentation. Warranty: 1-year limited manufacturer warranty. Suitable for users who prioritize camera quality, gaming stability, and long software support lifecycle.',
 1, 1, 8999.00, 11999.00, 268, 1680, 126, 392),

(9001, 8800, 7, 'Apple', 'MacBook Pro 14-inch M3 Pro', 'Portable workstation with high sustained performance.', '/cdn/images/61lsexTCOhL._AC_SX522_.jpg',
 'Configuration options include M3 Pro chip, 18GB or 36GB unified memory, and up to 1TB SSD. The 14.2-inch Liquid Retina XDR display reaches up to 1600 nits peak HDR brightness. Ports include HDMI, SDXC, three Thunderbolt 4, and MagSafe. Typical battery life can reach up to 18 hours for video playback based on manufacturer data. Recommended for software development, design workflows, and video post-production in a compact form factor.',
 1, 1, 14999.00, 18999.00, 112, 840, 71, 205),

(9002, 8800, 8, 'Sony', 'WH-1000XM5 Noise Cancelling Headphones', 'Industry-leading ANC with lightweight comfort.', '/cdn/images/AirPods Pro 2.jpg',
 'Dual processor noise cancellation adapts to office, commute, and flight conditions. Supports LDAC high-resolution wireless audio and multi-device pairing. Up to 30 hours battery life with ANC enabled, plus quick charge that provides around 3 hours playback in 3 minutes. Ear cushions are designed for long-session wear. Suitable for remote work, travel, and music listeners who need strong voice call clarity and stable Bluetooth connectivity.',
 1, 1, 2299.00, 2599.00, 356, 2210, 149, 467),

(9003, 8800, 10, NULL, 'Silk Blend Midi Dress', 'Breathable lining, tailored waist, and daily-to-event versatility.', '/cdn/images/IrisSwarovskiSilkEveningDress-8_1024x.jpg',
 'Fabric composition: 62% silk, 34% viscose, 4% elastane. Features include concealed back zipper, wrinkle-resistant lining, and reinforced shoulder seams for shape retention. Care guidance: cold gentle hand wash or professional dry clean, avoid high-temperature tumble drying. Fit profile favors a defined waist with moderate stretch. Recommended for office occasions, social dinner events, and spring-autumn commuting.',
 1, 1, 499.00, 559.00, 189, 1330, 92, 248),

(9004, 8800, 4, NULL, 'Clean Code (Chinese Edition)', 'Practical software craftsmanship guide with case-driven chapters.', '/cdn/images/59c22690N479e81.jpg',
 'Covers naming standards, function granularity, error handling strategies, testability, and refactoring paths. Includes common anti-pattern discussions and readability-driven coding conventions. Paperback and hardcover versions are available for different reading preferences. Recommended for junior-to-senior engineers, team leads, and reviewers building maintainable long-term codebases.',
 1, 1, 79.00, 129.00, 641, 4120, 313, 802),

(9005, 8800, 3, 'Dyson', 'Dyson V12 Detect Slim', 'Laser dust detection with lightweight cordless design.', '/cdn/images/OIP-C.jpg',
 'Provides real-time particle count feedback and automatic suction adjustment based on floor type. Includes fluffy roller head, anti-tangle motorbar head, crevice tool, and wall dock. Runtime up to 60 minutes in eco mode with swappable battery compatibility depending on package. Designed for apartment cleaning, pet hair collection, and hard-floor dust visibility.',
 1, 1, 3899.00, 4299.00, 74, 920, 58, 144);

-- 7. SKU data
INSERT INTO product_sku (
    id, spu_id, sku_code, sku_name, sale_price, origin_price, stock, lock_stock, warning_stock,
    image_url, spec_json, status, version
) VALUES
(9100, 9000, 'Buyer-SKU-9100', 'iPhone 15 Pro Max 256GB Natural Titanium', 8999.00, 9999.00, 40, 0, 8, '/cdn/images/9d950947-710e-4e14-8092-7c880689d50c.jpg', '{"storage":"256GB","color":"Natural Titanium"}', 1, 0),
(9101, 9000, 'Buyer-SKU-9101', 'iPhone 15 Pro Max 512GB Blue Titanium', 10499.00, 11499.00, 28, 0, 8, '/cdn/images/9d950947-710e-4e14-8092-7c880689d50c.jpg', '{"storage":"512GB","color":"Blue Titanium"}', 1, 0),
(9102, 9000, 'Buyer-SKU-9102', 'iPhone 15 Pro Max 1TB Black Titanium', 11999.00, 12999.00, 16, 0, 6, '/cdn/images/9d950947-710e-4e14-8092-7c880689d50c.jpg', '{"storage":"1TB","color":"Black Titanium"}', 1, 0),

(9103, 9001, 'Buyer-SKU-9103', 'MacBook Pro 14-inch M3 Pro 18GB/512GB Space Black', 14999.00, 15999.00, 18, 0, 5, '/cdn/images/61lsexTCOhL._AC_SX522_.jpg', '{"chip":"M3 Pro","memory":"18GB","storage":"512GB","color":"Space Black"}', 1, 0),
(9104, 9001, 'Buyer-SKU-9104', 'MacBook Pro 14-inch M3 Pro 36GB/1TB Silver', 18999.00, 19999.00, 10, 0, 4, '/cdn/images/61lsexTCOhL._AC_SX522_.jpg', '{"chip":"M3 Pro","memory":"36GB","storage":"1TB","color":"Silver"}', 1, 0),

(9105, 9002, 'Buyer-SKU-9105', 'Sony WH-1000XM5 Black', 2299.00, 2599.00, 55, 0, 10, '/cdn/images/AirPods Pro 2.jpg', '{"color":"Black"}', 1, 0),
(9106, 9002, 'Buyer-SKU-9106', 'Sony WH-1000XM5 Silver', 2399.00, 2599.00, 44, 0, 10, '/cdn/images/AirPods Pro 2.jpg', '{"color":"Silver"}', 1, 0),

(9107, 9003, 'Buyer-SKU-9107', 'Silk Blend Midi Dress M Navy', 499.00, 559.00, 72, 0, 12, '/cdn/images/IrisSwarovskiSilkEveningDress-8_1024x.jpg', '{"size":"M","color":"Navy"}', 1, 0),
(9108, 9004, 'Buyer-SKU-9108', 'Clean Code Chinese Edition Paperback', 79.00, 99.00, 160, 0, 15, '/cdn/images/59c22690N479e81.jpg', '{"format":"Paperback"}', 1, 0),
(9109, 9005, 'Buyer-SKU-9109', 'Dyson V12 Detect Slim Standard Set', 3899.00, 4299.00, 24, 0, 5, '/cdn/images/OIP-C.jpg', '{"bundle":"Standard"}', 1, 0);

-- 8. Product images (all under /cdn/images, using existing files)
INSERT INTO product_image (id, spu_id, sku_id, image_url, image_type, sort_order, create_time) VALUES
(9200, 9000, NULL, '/cdn/images/9d950947-710e-4e14-8092-7c880689d50c.jpg', 1, 0, '2026-03-31 09:00:00'),
(9201, 9000, NULL, '/cdn/images/9d950947-710e-4e14-8092-7c880689d50c.jpg', 2, 1, '2026-03-31 09:00:10'),
(9202, 9001, NULL, '/cdn/images/61lsexTCOhL._AC_SX522_.jpg', 1, 0, '2026-03-31 09:01:00'),
(9203, 9002, NULL, '/cdn/images/AirPods Pro 2.jpg', 1, 0, '2026-03-31 09:02:00'),
(9204, 9003, NULL, '/cdn/images/IrisSwarovskiSilkEveningDress-8_1024x.jpg', 1, 0, '2026-03-31 09:03:00'),
(9205, 9004, NULL, '/cdn/images/59c22690N479e81.jpg', 1, 0, '2026-03-31 09:04:00'),
(9206, 9005, NULL, '/cdn/images/OIP-C.jpg', 1, 0, '2026-03-31 09:05:00'),
(9207, 9000, 9100, '/cdn/images/9d950947-710e-4e14-8092-7c880689d50c.jpg', 3, 0, '2026-03-31 09:05:30'),
(9208, 9001, 9103, '/cdn/images/61lsexTCOhL._AC_SX522_.jpg', 3, 0, '2026-03-31 09:06:00'),
(9209, 9002, 9105, '/cdn/images/AirPods Pro 2.jpg', 3, 0, '2026-03-31 09:06:30');

-- 9. Optional profile data for buyer
INSERT INTO user_address (
    id, user_id, receiver_name, receiver_phone, province, city, district,
    detail_address, postal_code, is_default, tag_name
) VALUES
(9300, 7700, 'Lee', '13900007777', 'Guangdong', 'Shenzhen', 'Nanshan', 'No. 188, Keji South 12th Road, Building A, Room 1203', '518000', 1, 'Home');

-- 10. Favorites for buyer (multiple records)
INSERT INTO user_favorite (id, user_id, spu_id, create_time) VALUES
(9400, 7700, 9000, '2026-03-30 09:20:00'),
(9401, 7700, 9001, '2026-03-30 09:42:00'),
(9402, 7700, 9002, '2026-03-30 10:05:00'),
(9403, 7700, 9004, '2026-03-30 21:18:00'),
(9404, 7700, 9005, '2026-03-31 08:12:00');

-- 11. Browse history for buyer (multiple records)
INSERT INTO user_browse_history (id, user_id, spu_id, browse_time) VALUES
(9500, 7700, 9000, '2026-03-29 20:01:10'),
(9501, 7700, 9002, '2026-03-29 20:03:52'),
(9502, 7700, 9001, '2026-03-29 20:08:17'),
(9503, 7700, 9003, '2026-03-29 20:14:45'),
(9504, 7700, 9000, '2026-03-30 09:16:04'),
(9505, 7700, 9005, '2026-03-30 11:40:26'),
(9506, 7700, 9004, '2026-03-30 14:22:58'),
(9507, 7700, 9002, '2026-03-30 18:37:13'),
(9508, 7700, 9001, '2026-03-30 22:05:49'),
(9509, 7700, 9003, '2026-03-31 08:02:11'),
(9510, 7700, 9005, '2026-03-31 08:09:33'),
(9511, 7700, 9000, '2026-03-31 08:34:55');

-- 12. Optional cart sample for buyer
INSERT INTO cart_item (id, user_id, sku_id, quantity, checked) VALUES
(9600, 7700, 9100, 1, 1),
(9601, 7700, 9105, 1, 1),
(9602, 7700, 9108, 2, 1);

-- 13. Product reviews (detailed, multiple per product)
INSERT INTO product_review (
    id, order_item_id, order_no, user_id, spu_id, sku_id,
    score, content, anonymous_flag, review_status, create_time
) VALUES
-- iPhone 15 Pro Max (4)
(9700, 9800, 'ORD-20260330-001', 7700, 9000, 9100, 5, 'Camera is sharp with strong low-light detail. ProMotion feels smooth and battery lasts a full day of mixed use. USB-C charging is convenient; build is solid and balanced.', 0, 1, '2026-03-30 10:10:00'),
(9701, 9801, 'ORD-20260330-002', 7700, 9000, 9101, 4, 'Display is bright and color-accurate. Thermals stay stable during long video recording. Price is steep but overall experience is premium.', 1, 1, '2026-03-30 12:45:00'),
(9702, 9802, 'ORD-20260330-003', 7700, 9000, 9102, 5, 'Gaming performance is excellent; no stutter in heavy titles. Speakers are loud and clear. Battery health still great after a week of heavy use.', 0, 1, '2026-03-30 14:30:00'),
(9703, 9803, 'ORD-20260330-004', 7700, 9000, 9100, 4, 'Photos have good dynamic range and skin tones look natural. The device is a bit heavy with a case, but build quality is top tier.', 0, 1, '2026-03-30 18:05:00'),
-- MacBook Pro 14-inch M3 Pro (4)
(9704, 9804, 'ORD-20260330-005', 7700, 9001, 9103, 5, 'Compiles are fast and the fan rarely spins up. Keyboard is comfortable for long sessions. Screen is crisp with deep contrast.', 0, 1, '2026-03-30 15:20:00'),
(9705, 9805, 'ORD-20260330-006', 7700, 9001, 9104, 4, 'Excellent display and speaker quality. Battery life is solid for real work. A bit pricey but the performance is consistent.', 0, 1, '2026-03-30 16:05:00'),
(9706, 9806, 'ORD-20260330-007', 7700, 9001, 9103, 5, 'Runs multiple design apps smoothly. Ports are practical and MagSafe is a plus. Stays cool on lap even during exports.', 1, 1, '2026-03-30 17:40:00'),
(9707, 9807, 'ORD-20260330-008', 7700, 9001, 9104, 4, 'Trackpad is precise and large. Wi-Fi performance is stable. Slightly heavy for daily commute but acceptable.', 0, 1, '2026-03-30 20:15:00'),
-- Sony WH-1000XM5 (4)
(9708, 9808, 'ORD-20260330-009', 7700, 9002, 9105, 5, 'ANC is very effective on subway and office noise. Bass is tight, mids are clear. Mic quality is good for calls.', 0, 1, '2026-03-30 18:30:00'),
(9709, 9809, 'ORD-20260330-010', 7700, 9002, 9106, 4, 'Comfortable for 2-3 hour sessions and not too hot. Multipoint works well. Case is compact and easy to carry.', 1, 1, '2026-03-30 19:10:00'),
(9710, 9810, 'ORD-20260330-011', 7700, 9002, 9105, 5, 'Battery easily lasts a full workweek. Controls are responsive and touch gestures are intuitive.', 0, 1, '2026-03-30 21:10:00'),
(9711, 9811, 'ORD-20260330-012', 7700, 9002, 9106, 4, 'Soundstage is wider than expected. ANC pressure is mild; good balance for long use.', 0, 1, '2026-03-30 22:10:00'),
-- Silk Blend Midi Dress (4)
(9712, 9812, 'ORD-20260330-013', 7700, 9003, 9107, 5, 'Fabric feels premium and drapes nicely. Fit is flattering at the waist with enough stretch for comfort.', 0, 1, '2026-03-30 20:05:00'),
(9713, 9813, 'ORD-20260330-014', 7700, 9003, 9107, 4, 'Color is true to photos and stitching is clean. Lining is breathable and not itchy.', 0, 1, '2026-03-30 21:15:00'),
(9714, 9814, 'ORD-20260330-015', 7700, 9003, 9107, 5, 'Works well for office and dinner. Zipper is smooth and the hemline falls nicely.', 1, 1, '2026-03-30 22:05:00'),
(9715, 9815, 'ORD-20260330-016', 7700, 9003, 9107, 4, 'Slightly long for shorter height but easy to adjust with heels. Overall quality is good.', 0, 1, '2026-03-31 08:20:00'),
-- Clean Code (Chinese Edition) (4)
(9716, 9816, 'ORD-20260330-017', 7700, 9004, 9108, 5, 'Practical advice with clear examples. The chapter on naming is especially useful for reviews.', 0, 1, '2026-03-30 22:00:00'),
(9717, 9817, 'ORD-20260330-018', 7700, 9004, 9108, 4, 'Good reference for refactoring discussions. Some sections are dense but worth reading.', 1, 1, '2026-03-30 22:40:00'),
(9718, 9818, 'ORD-20260330-019', 7700, 9004, 9108, 5, 'Examples are easy to follow. Helps establish shared standards within a team.', 0, 1, '2026-03-31 09:05:00'),
(9719, 9819, 'ORD-20260330-020', 7700, 9004, 9108, 4, 'Print quality is good. Great for junior engineers to build habits.', 0, 1, '2026-03-31 09:25:00'),
-- Dyson V12 Detect Slim (4)
(9720, 9820, 'ORD-20260330-021', 7700, 9005, 9109, 5, 'Laser dust detection is surprisingly useful. Lightweight and easy to maneuver under furniture.', 0, 1, '2026-03-31 09:10:00'),
(9721, 9821, 'ORD-20260330-022', 7700, 9005, 9109, 4, 'Suction is strong on hard floors and medium on rugs. Noise level is acceptable.', 0, 1, '2026-03-31 09:35:00'),
(9722, 9822, 'ORD-20260330-023', 7700, 9005, 9109, 5, 'Battery lasts around 45 minutes in eco mode. Dust bin is easy to empty.', 1, 1, '2026-03-31 10:05:00'),
(9723, 9823, 'ORD-20260330-024', 7700, 9005, 9109, 4, 'Accessories are useful, especially the crevice tool. Good overall value for apartments.', 0, 1, '2026-03-31 10:25:00');


