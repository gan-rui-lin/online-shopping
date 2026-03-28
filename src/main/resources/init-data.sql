-- =============================================
-- Online Shopping Platform - Demo Data
-- =============================================
-- This file inserts comprehensive demo data for development and testing.
-- All user passwords are BCrypt-encoded "123456".
-- Uses INSERT IGNORE to safely re-run without duplicate errors.
-- Intended to be run AFTER schema.sql has created all tables.
-- =============================================

USE online_shopping;

-- =============================================
-- 1. Users
-- =============================================
-- Buyers: id 100-102, Merchants: id 103-105

INSERT IGNORE INTO sys_user (id, username, password, nickname, phone, user_type, status) VALUES
(100, 'buyer1',    '$2a$10$77fqFla9dMpEi4DR1J8WBO6qaJKrR8WChf8I5jwugvpkVseMI1yBe', 'Alice Wang',  '13800100001', 1, 1),
(101, 'buyer2',    '$2a$10$77fqFla9dMpEi4DR1J8WBO6qaJKrR8WChf8I5jwugvpkVseMI1yBe', 'Bob Li',      '13800100002', 1, 1),
(102, 'buyer3',    '$2a$10$77fqFla9dMpEi4DR1J8WBO6qaJKrR8WChf8I5jwugvpkVseMI1yBe', 'Carol Zhang', '13800100003', 1, 1),
(103, 'merchant1', '$2a$10$77fqFla9dMpEi4DR1J8WBO6qaJKrR8WChf8I5jwugvpkVseMI1yBe', 'David Chen',  '13800100004', 2, 1),
(104, 'merchant2', '$2a$10$77fqFla9dMpEi4DR1J8WBO6qaJKrR8WChf8I5jwugvpkVseMI1yBe', 'Emily Liu',   '13800100005', 2, 1),
(105, 'merchant3', '$2a$10$77fqFla9dMpEi4DR1J8WBO6qaJKrR8WChf8I5jwugvpkVseMI1yBe', 'Frank Wu',    '13800100006', 2, 1);

-- User-Role assignments: all buyers get ROLE_BUYER(1), merchants get ROLE_BUYER(1) AND ROLE_MERCHANT(2)
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(100, 1),
(101, 1),
(102, 1),
(103, 1), (103, 2),
(104, 1), (104, 2),
(105, 1), (105, 2);

-- =============================================
-- 2. Shops
-- =============================================

INSERT IGNORE INTO merchant_shop (id, user_id, shop_name, shop_desc, shop_status, score) VALUES
(100, 103, 'David''s Electronics', 'Premium electronics and gadgets',   1, 4.80),
(101, 104, 'Emily''s Fashion',     'Stylish clothing for every season', 1, 4.90),
(102, 105, 'Frank''s Books',       'Books for curious minds',           1, 4.70);

-- =============================================
-- 3. Products — SPU (12 products)
-- =============================================

-- David's Electronics (shop_id=100)
INSERT IGNORE INTO product_spu (id, shop_id, category_id, brand_name, title, sub_title, status, audit_status, min_price, max_price, sales_count, browse_count, favorite_count, like_count) VALUES
(1000, 100, 6,  'Apple',   'iPhone 15 Pro Max',       'Titanium. A17 Pro chip. Action button.',     1, 1, 1199.00, 1599.00, 320, 5800, 210, 450),
(1001, 100, 7,  'Apple',   'MacBook Pro 14 M3',       'Supercharged by M3 Pro or M3 Max.',          1, 1, 1999.00, 2999.00, 180, 4200, 150, 320),
(1002, 100, 8,  'Apple',   'AirPods Pro 2',           'Adaptive Audio. USB-C charging.',             1, 1,  249.00,  249.00, 560, 8200, 380, 620),
(1003, 100, 6,  'Samsung', 'Samsung Galaxy S24 Ultra', 'Galaxy AI. Titanium frame. S Pen built-in.', 1, 1, 1299.00, 1419.00, 240, 3900, 130, 280);

-- Emily's Fashion (shop_id=101)
INSERT IGNORE INTO product_spu (id, shop_id, category_id, brand_name, title, sub_title, status, audit_status, min_price, max_price, sales_count, browse_count, favorite_count, like_count) VALUES
(1004, 101, 9,  NULL, 'Classic Wool Overcoat', 'Warm and elegant for autumn and winter.',          1, 1, 189.00, 199.00,  95, 1800,  55, 110),
(1005, 101, 10, NULL, 'Silk Evening Dress',    'Luxurious silk, perfect for formal occasions.',    1, 1, 299.00, 319.00,  72, 2100,  65, 130),
(1006, 101, 9,  NULL, 'Casual Denim Jacket',   'Classic denim, versatile and durable.',            1, 1,  89.00,  89.00, 210, 3400, 120, 250),
(1007, 101, 10, NULL, 'Cashmere Sweater',      'Soft 100% cashmere, lightweight and cozy.',        1, 1, 159.00, 159.00, 130, 2500,  90, 180);

-- Frank's Books (shop_id=102)
INSERT IGNORE INTO product_spu (id, shop_id, category_id, brand_name, title, sub_title, status, audit_status, min_price, max_price, sales_count, browse_count, favorite_count, like_count) VALUES
(1008, 102, 4, NULL, 'Clean Code',                       'A Handbook of Agile Software Craftsmanship by Robert C. Martin.', 1, 1,  35.00,  55.00, 420, 6100, 280, 510),
(1009, 102, 4, NULL, 'Design Patterns GoF',              'Elements of Reusable Object-Oriented Software.',                  1, 1,  42.00,  65.00, 310, 4500, 200, 380),
(1010, 102, 4, NULL, 'The Art of War',                   'Sun Tzu''s timeless classic on strategy.',                         1, 1,  12.00,  28.00, 580, 7200, 350, 600),
(1011, 102, 4, NULL, 'Introduction to Algorithms CLRS',  'The comprehensive guide to algorithms by Cormen et al.',           1, 1,  70.00,  95.00, 190, 3800, 140, 260);

-- =============================================
-- 3b. Products — SKU (26 SKUs)
-- =============================================

-- iPhone 15 Pro Max (spu_id=1000)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2000, 1000, 'SKU2000', 'iPhone 15 Pro Max 256GB Natural Titanium', 1199.00, 50,  0, 10, '{"storage":"256GB","color":"Natural Titanium"}', 0),
(2001, 1000, 'SKU2001', 'iPhone 15 Pro Max 512GB Blue Titanium',    1399.00, 50,  0, 10, '{"storage":"512GB","color":"Blue Titanium"}',    0),
(2002, 1000, 'SKU2002', 'iPhone 15 Pro Max 1TB Black Titanium',     1599.00, 30,  0, 10, '{"storage":"1TB","color":"Black Titanium"}',     0);

-- MacBook Pro 14 M3 (spu_id=1001)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2003, 1001, 'SKU2003', 'MacBook Pro 14 M3 16GB/512GB Silver',      1999.00, 30, 0, 10, '{"memory":"16GB","storage":"512GB","color":"Silver"}',      0),
(2004, 1001, 'SKU2004', 'MacBook Pro 14 M3 32GB/1TB Space Black',   2999.00, 20, 0, 10, '{"memory":"32GB","storage":"1TB","color":"Space Black"}',   0);

-- AirPods Pro 2 (spu_id=1002)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2005, 1002, 'SKU2005', 'AirPods Pro 2 White', 249.00, 200, 0, 10, '{"color":"White"}', 0);

-- Samsung Galaxy S24 Ultra (spu_id=1003)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2006, 1003, 'SKU2006', 'Samsung Galaxy S24 Ultra 256GB Titanium Gray',  1299.00, 40, 0, 10, '{"storage":"256GB","color":"Titanium Gray"}',  0),
(2007, 1003, 'SKU2007', 'Samsung Galaxy S24 Ultra 512GB Titanium Black', 1419.00, 40, 0, 10, '{"storage":"512GB","color":"Titanium Black"}', 0);

-- Classic Wool Overcoat (spu_id=1004)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2008, 1004, 'SKU2008', 'Classic Wool Overcoat M Black',  189.00, 60, 0, 10, '{"size":"M","color":"Black"}', 0),
(2009, 1004, 'SKU2009', 'Classic Wool Overcoat L Black',  189.00, 60, 0, 10, '{"size":"L","color":"Black"}', 0),
(2010, 1004, 'SKU2010', 'Classic Wool Overcoat XL Gray',  199.00, 40, 0, 10, '{"size":"XL","color":"Gray"}', 0);

-- Silk Evening Dress (spu_id=1005)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2011, 1005, 'SKU2011', 'Silk Evening Dress S Red',       299.00, 40, 0, 10, '{"size":"S","color":"Red"}',       0),
(2012, 1005, 'SKU2012', 'Silk Evening Dress M Navy',      299.00, 40, 0, 10, '{"size":"M","color":"Navy"}',      0),
(2013, 1005, 'SKU2013', 'Silk Evening Dress L Champagne', 319.00, 30, 0, 10, '{"size":"L","color":"Champagne"}', 0);

-- Casual Denim Jacket (spu_id=1006)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2014, 1006, 'SKU2014', 'Casual Denim Jacket M Blue', 89.00, 80, 0, 10, '{"size":"M","color":"Blue"}', 0),
(2015, 1006, 'SKU2015', 'Casual Denim Jacket L Blue', 89.00, 80, 0, 10, '{"size":"L","color":"Blue"}', 0);

-- Cashmere Sweater (spu_id=1007)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2016, 1007, 'SKU2016', 'Cashmere Sweater S Cream', 159.00, 70, 0, 10, '{"size":"S","color":"Cream"}', 0),
(2017, 1007, 'SKU2017', 'Cashmere Sweater M Pink',  159.00, 70, 0, 10, '{"size":"M","color":"Pink"}',  0);

-- Clean Code (spu_id=1008)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2018, 1008, 'SKU2018', 'Clean Code Paperback', 35.00, 100, 0, 10, '{"format":"Paperback"}', 0),
(2019, 1008, 'SKU2019', 'Clean Code Hardcover', 55.00,  80, 0, 10, '{"format":"Hardcover"}', 0);

-- Design Patterns GoF (spu_id=1009)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2020, 1009, 'SKU2020', 'Design Patterns GoF Paperback', 42.00, 80, 0, 10, '{"format":"Paperback"}', 0),
(2021, 1009, 'SKU2021', 'Design Patterns GoF Hardcover', 65.00, 60, 0, 10, '{"format":"Hardcover"}', 0);

-- The Art of War (spu_id=1010)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2022, 1010, 'SKU2022', 'The Art of War Paperback', 12.00, 150, 0, 10, '{"format":"Paperback"}', 0),
(2023, 1010, 'SKU2023', 'The Art of War Hardcover', 28.00, 100, 0, 10, '{"format":"Hardcover"}', 0);

-- Introduction to Algorithms CLRS (spu_id=1011)
INSERT IGNORE INTO product_sku (id, spu_id, sku_code, sku_name, sale_price, stock, lock_stock, warning_stock, spec_json, version) VALUES
(2024, 1011, 'SKU2024', 'Introduction to Algorithms CLRS Paperback', 70.00, 60, 0, 10, '{"format":"Paperback"}', 0),
(2025, 1011, 'SKU2025', 'Introduction to Algorithms CLRS Hardcover', 95.00, 40, 0, 10, '{"format":"Hardcover"}', 0);

-- =============================================
-- 4. Addresses (2 per buyer, realistic Chinese cities)
-- =============================================

INSERT IGNORE INTO user_address (id, user_id, receiver_name, receiver_phone, province, city, district, detail_address, postal_code, is_default, tag_name) VALUES
-- buyer1 (Alice Wang)
(100, 100, 'Alice Wang', '13800100001', 'Beijing',   'Beijing',   'Haidian',  'Zhongguancun South Street No. 5, Building 8, Unit 301',  '100081', 1, 'Home'),
(101, 100, 'Alice Wang', '13800100001', 'Shanghai',  'Shanghai',  'Pudong',   'Century Avenue No. 100, Oriental Manhattan Tower 2-1502', '200120', 0, 'Work'),
-- buyer2 (Bob Li)
(102, 101, 'Bob Li',     '13800100002', 'Guangdong', 'Guangzhou', 'Tianhe',   'Tianhe Road No. 385, Taikoo Hui Residence 22F',           '510620', 1, 'Home'),
(103, 101, 'Bob Li',     '13800100002', 'Guangdong', 'Shenzhen',  'Nanshan',  'Keyuan Road South No. 12, Shenzhen Bay Eco-Tech Park',    '518057', 0, 'Office'),
-- buyer3 (Carol Zhang)
(104, 102, 'Carol Zhang','13800100003', 'Zhejiang',  'Hangzhou',  'Xihu',     'Longjing Road No. 88, West Lake Garden Villa 6',          '310013', 1, 'Home'),
(105, 102, 'Carol Zhang','13800100003', 'Sichuan',   'Chengdu',   'Wuhou',    'Renmin South Road Section 4 No. 48, Yingtai International','610041', 0, 'Parents');

-- =============================================
-- 5. Completed Orders (3 orders, status=3 completed, pay_status=1 paid)
-- =============================================

-- Order 1: buyer1 (Alice) bought iPhone 15 Pro Max 256GB x1 from David's Electronics
INSERT IGNORE INTO order_info (id, order_no, user_id, shop_id, total_amount, discount_amount, pay_amount, freight_amount, order_status, pay_status, source_type, receiver_name, receiver_phone, receiver_address, pay_time, delivery_time, finish_time, create_time) VALUES
(100, '20260301100001', 100, 100, 1199.00, 0.00, 1199.00, 0.00, 3, 1, 1, 'Alice Wang', '13800100001', 'Beijing Beijing Haidian Zhongguancun South Street No. 5, Building 8, Unit 301', '2026-03-01 10:05:00', '2026-03-02 09:00:00', '2026-03-05 14:30:00', '2026-03-01 10:00:00');

INSERT IGNORE INTO order_item (id, order_id, order_no, spu_id, sku_id, product_title, sku_name, sku_spec_json, sale_price, quantity, total_amount, review_status) VALUES
(100, 100, '20260301100001', 1000, 2000, 'iPhone 15 Pro Max', 'iPhone 15 Pro Max 256GB Natural Titanium', '{"storage":"256GB","color":"Natural Titanium"}', 1199.00, 1, 1199.00, 1);

INSERT IGNORE INTO payment_record (id, order_no, pay_no, user_id, pay_amount, pay_method, pay_status, pay_time, create_time) VALUES
(100, '20260301100001', 'PAY20260301100001', 100, 1199.00, 1, 1, '2026-03-01 10:05:00', '2026-03-01 10:05:00');

INSERT IGNORE INTO order_operate_log (id, order_id, order_no, before_status, after_status, operator_id, operator_role, operate_type, remark, operate_time) VALUES
(100, 100, '20260301100001', NULL, 0, 100, 'BUYER',    'CREATE',   'Order created',   '2026-03-01 10:00:00'),
(101, 100, '20260301100001', 0,    1, 100, 'BUYER',    'PAY',      'Payment success',  '2026-03-01 10:05:00'),
(102, 100, '20260301100001', 1,    2, 103, 'MERCHANT', 'SHIP',     'Order shipped',    '2026-03-02 09:00:00'),
(103, 100, '20260301100001', 2,    3, 100, 'BUYER',    'CONFIRM',  'Delivery confirmed','2026-03-05 14:30:00');

-- Order 2: buyer2 (Bob) bought Clean Code Paperback x1 + Art of War Paperback x1 from Frank's Books
INSERT IGNORE INTO order_info (id, order_no, user_id, shop_id, total_amount, discount_amount, pay_amount, freight_amount, order_status, pay_status, source_type, receiver_name, receiver_phone, receiver_address, pay_time, delivery_time, finish_time, create_time) VALUES
(101, '20260305150002', 101, 102, 47.00, 0.00, 47.00, 0.00, 3, 1, 1, 'Bob Li', '13800100002', 'Guangdong Guangzhou Tianhe Tianhe Road No. 385, Taikoo Hui Residence 22F', '2026-03-05 15:10:00', '2026-03-06 10:00:00', '2026-03-09 11:00:00', '2026-03-05 15:00:00');

INSERT IGNORE INTO order_item (id, order_id, order_no, spu_id, sku_id, product_title, sku_name, sku_spec_json, sale_price, quantity, total_amount, review_status) VALUES
(101, 101, '20260305150002', 1008, 2018, 'Clean Code',     'Clean Code Paperback',     '{"format":"Paperback"}', 35.00, 1, 35.00, 1),
(102, 101, '20260305150002', 1010, 2022, 'The Art of War', 'The Art of War Paperback',  '{"format":"Paperback"}', 12.00, 1, 12.00, 1);

INSERT IGNORE INTO payment_record (id, order_no, pay_no, user_id, pay_amount, pay_method, pay_status, pay_time, create_time) VALUES
(101, '20260305150002', 'PAY20260305150002', 101, 47.00, 1, 1, '2026-03-05 15:10:00', '2026-03-05 15:10:00');

INSERT IGNORE INTO order_operate_log (id, order_id, order_no, before_status, after_status, operator_id, operator_role, operate_type, remark, operate_time) VALUES
(104, 101, '20260305150002', NULL, 0, 101, 'BUYER',    'CREATE',   'Order created',    '2026-03-05 15:00:00'),
(105, 101, '20260305150002', 0,    1, 101, 'BUYER',    'PAY',      'Payment success',  '2026-03-05 15:10:00'),
(106, 101, '20260305150002', 1,    2, 105, 'MERCHANT', 'SHIP',     'Order shipped',    '2026-03-06 10:00:00'),
(107, 101, '20260305150002', 2,    3, 101, 'BUYER',    'CONFIRM',  'Delivery confirmed','2026-03-09 11:00:00');

-- Order 3: buyer3 (Carol) bought Silk Evening Dress M Navy x1 from Emily's Fashion
INSERT IGNORE INTO order_info (id, order_no, user_id, shop_id, total_amount, discount_amount, pay_amount, freight_amount, order_status, pay_status, source_type, receiver_name, receiver_phone, receiver_address, pay_time, delivery_time, finish_time, create_time) VALUES
(102, '20260308120003', 102, 101, 299.00, 0.00, 299.00, 0.00, 3, 1, 1, 'Carol Zhang', '13800100003', 'Zhejiang Hangzhou Xihu Longjing Road No. 88, West Lake Garden Villa 6', '2026-03-08 12:10:00', '2026-03-09 08:30:00', '2026-03-12 16:00:00', '2026-03-08 12:00:00');

INSERT IGNORE INTO order_item (id, order_id, order_no, spu_id, sku_id, product_title, sku_name, sku_spec_json, sale_price, quantity, total_amount, review_status) VALUES
(103, 102, '20260308120003', 1005, 2012, 'Silk Evening Dress', 'Silk Evening Dress M Navy', '{"size":"M","color":"Navy"}', 299.00, 1, 299.00, 1);

INSERT IGNORE INTO payment_record (id, order_no, pay_no, user_id, pay_amount, pay_method, pay_status, pay_time, create_time) VALUES
(102, '20260308120003', 'PAY20260308120003', 102, 299.00, 1, 1, '2026-03-08 12:10:00', '2026-03-08 12:10:00');

INSERT IGNORE INTO order_operate_log (id, order_id, order_no, before_status, after_status, operator_id, operator_role, operate_type, remark, operate_time) VALUES
(108, 102, '20260308120003', NULL, 0, 102, 'BUYER',    'CREATE',   'Order created',    '2026-03-08 12:00:00'),
(109, 102, '20260308120003', 0,    1, 102, 'BUYER',    'PAY',      'Payment success',  '2026-03-08 12:10:00'),
(110, 102, '20260308120003', 1,    2, 104, 'MERCHANT', 'SHIP',     'Order shipped',    '2026-03-09 08:30:00'),
(111, 102, '20260308120003', 2,    3, 102, 'BUYER',    'CONFIRM',  'Delivery confirmed','2026-03-12 16:00:00');

-- =============================================
-- 6. Reviews
-- =============================================

-- buyer1 reviews iPhone 15 Pro Max
INSERT IGNORE INTO product_review (id, order_item_id, order_no, user_id, spu_id, sku_id, score, content, review_status, create_time) VALUES
(100, 100, '20260301100001', 100, 1000, 2000, 5, 'Amazing phone, great camera!', 1, '2026-03-05 15:00:00');

-- buyer2 reviews Clean Code
INSERT IGNORE INTO product_review (id, order_item_id, order_no, user_id, spu_id, sku_id, score, content, review_status, create_time) VALUES
(101, 101, '20260305150002', 101, 1008, 2018, 5, 'Must read for developers', 1, '2026-03-09 12:00:00');

-- buyer2 reviews The Art of War
INSERT IGNORE INTO product_review (id, order_item_id, order_no, user_id, spu_id, sku_id, score, content, review_status, create_time) VALUES
(102, 102, '20260305150002', 101, 1010, 2022, 4, 'Timeless wisdom', 1, '2026-03-09 12:05:00');

-- buyer3 reviews Silk Evening Dress
INSERT IGNORE INTO product_review (id, order_item_id, order_no, user_id, spu_id, sku_id, score, content, review_status, create_time) VALUES
(103, 103, '20260308120003', 102, 1005, 2012, 5, 'Beautiful, perfect fit!', 1, '2026-03-12 17:00:00');

-- =============================================
-- 7. Favorites
-- =============================================

INSERT IGNORE INTO user_favorite (id, user_id, spu_id, create_time) VALUES
-- buyer1: MacBook Pro, AirPods Pro
(100, 100, 1001, '2026-03-02 10:00:00'),
(101, 100, 1002, '2026-03-02 10:05:00'),
-- buyer2: Clean Code, Design Patterns, Cashmere Sweater
(102, 101, 1008, '2026-03-04 14:00:00'),
(103, 101, 1009, '2026-03-04 14:10:00'),
(104, 101, 1007, '2026-03-06 20:00:00');

-- =============================================
-- 8. Cart Items
-- =============================================

INSERT IGNORE INTO cart_item (id, user_id, sku_id, quantity, checked) VALUES
-- buyer1: Samsung Galaxy S24 Ultra 256GB x1, Casual Denim Jacket M Blue x1
(100, 100, 2006, 1, 1),
(101, 100, 2014, 1, 1),
-- buyer2: Introduction to Algorithms CLRS Hardcover x1
(102, 101, 2025, 1, 1);



-- =============================================
-- file_test: for testing file upload functionality
-- =============================================
INSERT INTO product_image (spu_id, image_url, image_type)
VALUES (1002, '/cdn/images/AirPods Pro 2.jpg', 1);
