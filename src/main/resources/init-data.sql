-- Active: 1775290046496@@127.0.0.1@3306@online_shopping
-- =============================================
-- Online Shopping Platform - Enhanced Realistic Data
-- Merged from src/test/resources/enhanced-data.sql, enhanced-data-part2.sql, enhanced-data-part3.sql
-- Keep original image URLs when provided by source data; use fallback only for newly added rows without images
-- =============================================

USE online_shopping;

SET FOREIGN_KEY_CHECKS = 0;

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

INSERT INTO sys_role (id, role_code, role_name, status) VALUES
(1, 'ROLE_BUYER', 'Buyer', 1),
(2, 'ROLE_MERCHANT', 'Merchant', 1),
(3, 'ROLE_ADMIN', 'Administrator', 1);

INSERT INTO sys_user (id, username, password, nickname, phone, user_type, status, email) VALUES
(1001, 'admin', '$2b$10$niGCU3kDKNTazTeduKI7mOWioGc8VKDGdTyiI1EeLUaSrA6fnEPPu', 'System Admin', '13800001001', 3, 1, 'admin@onlineshopping.com'),
(1002, 'auditor', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Product Auditor', '13800001002', 3, 1, 'auditor@onlineshopping.com'),
(2001, 'tech_store', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'TechMart Owner', '13800002001', 2, 1, 'owner@techmart.com'),
(2002, 'fashion_hub', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Fashion Hub', '13800002002', 2, 1, 'contact@fashionhub.com'),
(2003, 'book_corner', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Book Corner', '13800002003', 2, 1, 'info@bookcorner.com'),
(2004, 'home_deco', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Home Decor Pro', '13800002004', 2, 1, 'sales@homedeco.com'),
(2005, 'sports_gear', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Sports Gear Plus', '13800002005', 2, 1, 'team@sportsgear.com'),
(2006, 'organic_food', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Organic Food Co', '13800002006', 2, 1, 'hello@organicfood.com'),
(2007, 'toy_kingdom', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Toy Kingdom', '13800002007', 2, 1, 'support@toykingdom.com'),
(2008, 'beauty_world', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Beauty World', '13800002008', 2, 1, 'service@beautyworld.com'),
(3001, 'alice_chen', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Alice Chen', '13900003001', 1, 1, 'alice.chen@email.com'),
(3002, 'bob_wang', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Bob Wang', '13900003002', 1, 1, 'bob.wang@email.com'),
(3003, 'carol_li', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Carol Li', '13900003003', 1, 1, 'carol.li@email.com'),
(3004, 'david_zhang', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'David Zhang', '13900003004', 1, 1, 'david.zhang@email.com'),
(3005, 'emily_liu', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Emily Liu', '13900003005', 1, 1, 'emily.liu@email.com'),
(3006, 'frank_wu', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Frank Wu', '13900003006', 1, 1, 'frank.wu@email.com'),
(3007, 'grace_xu', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Grace Xu', '13900003007', 1, 1, 'grace.xu@email.com'),
(3008, 'henry_zhao', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Henry Zhao', '13900003008', 1, 1, 'henry.zhao@email.com'),
(3009, 'ivy_sun', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Ivy Sun', '13900003009', 1, 1, 'ivy.sun@email.com'),
(3010, 'jack_ma', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Jack Ma', '13900003010', 1, 1, 'jack.ma@email.com'),
(3011, 'kelly_huang', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Kelly Huang', '13900003011', 1, 1, 'kelly.huang@email.com'),
(3012, 'leo_tang', '$2b$10$d0wLdk1NrIwqL0K6cWpMM.HgtQyvvlweCIn68fgTQnPxgP/EGiZuS', 'Leo Tang', '13900003012', 1, 1, 'leo.tang@email.com');

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1001, 3), (1002, 3),
(2001, 2), (2002, 2), (2003, 2), (2004, 2), (2005, 2), (2006, 2), (2007, 2), (2008, 2),
(3001, 1), (3002, 1), (3003, 1), (3004, 1), (3005, 1), (3006, 1), (3007, 1), (3008, 1), (3009, 1), (3010, 1), (3011, 1), (3012, 1);

INSERT INTO product_category (id, parent_id, category_name, level, sort_order, status) VALUES
(1, 0, 'Electronics', 1, 1, 1),
(2, 0, 'Fashion & Apparel', 1, 2, 1),
(3, 0, 'Home & Living', 1, 3, 1),
(4, 0, 'Books & Education', 1, 4, 1),
(5, 0, 'Sports & Outdoors', 1, 5, 1),
(6, 0, 'Beauty & Personal Care', 1, 6, 1),
(7, 0, 'Food & Beverages', 1, 7, 1),
(8, 0, 'Toys & Baby', 1, 8, 1),
(11, 1, 'Smartphones', 2, 1, 1),
(12, 1, 'Laptops & Computers', 2, 2, 1),
(13, 1, 'Audio & Headphones', 2, 3, 1),
(14, 1, 'Cameras & Photography', 2, 4, 1),
(15, 1, 'Gaming', 2, 5, 1),
(21, 2, 'Womens Clothing', 2, 1, 1),
(22, 2, 'Mens Clothing', 2, 2, 1),
(23, 2, 'Shoes', 2, 3, 1),
(24, 2, 'Accessories', 2, 4, 1),
(31, 3, 'Furniture', 2, 1, 1),
(32, 3, 'Home Appliances', 2, 2, 1),
(33, 3, 'Kitchen & Dining', 2, 3, 1),
(34, 3, 'Home Decor', 2, 4, 1),
(41, 4, 'Technology', 2, 1, 1),
(42, 4, 'Business & Economics', 2, 2, 1),
(43, 4, 'Literature & Fiction', 2, 3, 1),
(44, 4, 'Education & Textbooks', 2, 4, 1),
(51, 5, 'Fitness Equipment', 2, 1, 1),
(52, 5, 'Outdoor Activities', 2, 2, 1),
(53, 5, 'Team Sports', 2, 3, 1),
(54, 5, 'Water Sports', 2, 4, 1);

INSERT INTO merchant_shop (id, user_id, shop_name, shop_desc, shop_status, score, create_time) VALUES
(5001, 2001, 'TechMart Official Store', 'Leading technology retailer with smartphones, laptops, and accessories.', 1, 4.85, '2024-01-15 09:00:00'),
(5002, 2002, 'Fashion Hub Boutique', 'Fashion destination for men and women with curated collections.', 1, 4.72, '2024-02-20 10:30:00'),
(5003, 2003, 'Book Corner', 'Independent bookstore for technical books and literature.', 1, 4.91, '2024-01-05 14:20:00'),
(5004, 2004, 'Home Decor Pro', 'Premium home furnishing and decor solutions.', 1, 4.68, '2024-03-10 11:45:00'),
(5005, 2005, 'Sports Gear Plus', 'Professional sports equipment and fitness gear.', 1, 4.77, '2024-02-28 08:15:00'),
(5006, 2006, 'Organic Food Co', 'Certified organic food products and health supplements.', 1, 4.83, '2024-03-01 07:30:00'),
(5007, 2007, 'Toy Kingdom', 'Educational toys and games for children of all ages.', 1, 4.79, '2024-02-15 16:00:00'),
(5008, 2008, 'Beauty World', 'Cosmetics and beauty products from premium brands.', 1, 4.74, '2024-03-05 13:25:00');

INSERT INTO product_spu (
    id, shop_id, category_id, brand_name, title, sub_title, main_image, detail_text,
    status, audit_status, min_price, max_price, sales_count, browse_count, favorite_count, like_count, create_time
) VALUES

-- Electronics - Smartphones (11)
(10001, 5001, 11, 'Apple', 'iPhone 15 Pro Max', 'A17 Pro chip, titanium design, professional photography system', '/cdn/images/iphone15promax.jpg',
 'Features the most advanced iPhone camera system with 48MP main camera, 5x Telephoto camera, and Ultra Wide camera. A17 Pro chip delivers incredible performance and efficiency. Titanium design with Action Button for quick access to favorite features.',
 1, 1, 8999.00, 12999.00, 445, 2890, 312, 567, '2024-03-15 10:00:00'),

(10002, 5001, 11, 'Samsung', 'Galaxy S24 Ultra', 'AI-powered smartphone with S Pen and 200MP camera', '/cdn/images/galaxys24ultra.jpg',
 'Galaxy AI transforms your mobile experience with intelligent photo editing, real-time translation, and smart text assistance. 200MP camera with Space Zoom and enhanced night mode. Built-in S Pen for productivity on the go.',
 1, 1, 7999.00, 10999.00, 332, 2156, 289, 445, '2024-03-18 11:15:00'),

(10003, 5001, 11, 'Xiaomi', 'Mi 14 Pro', 'Leica camera system with Snapdragon 8 Gen 3', '/cdn/images/mi14pro.jpg',
 'Co-engineered with Leica for professional photography experience. Snapdragon 8 Gen 3 processor for flagship performance. 120W fast charging and wireless charging support.',
 1, 1, 4999.00, 6999.00, 578, 3445, 423, 691, '2024-03-20 14:30:00'),

-- Electronics - Laptops (12)
(10004, 5001, 12, 'Apple', 'MacBook Air M3', '13-inch laptop with M3 chip and all-day battery life', '/cdn/images/macbookairm3.jpg',
 'Revolutionary M3 chip delivers incredible performance in an ultra-thin design. Up to 18 hours of battery life. Stunning 13.6-inch Liquid Retina display with 1 billion colors.',
 1, 1, 8999.00, 12999.00, 267, 1890, 198, 334, '2024-03-12 09:20:00'),

(10005, 5001, 12, 'Dell', 'XPS 15 OLED', 'Premium laptop with 4K OLED display and RTX graphics', '/cdn/images/dellxps15.jpg',
 'Stunning 15.6-inch 4K OLED InfinityEdge display with 100% DCI-P3 color coverage. NVIDIA GeForce RTX 4070 graphics for creative work and gaming. Premium carbon fiber and aluminum construction.',
 1, 1, 12999.00, 18999.00, 156, 1234, 145, 223, '2024-03-14 15:45:00'),

(10006, 5001, 12, 'Lenovo', 'ThinkPad X1 Carbon', 'Business laptop with Intel Core i7 and lightweight design', '/cdn/images/thinkpadx1.jpg',
 'Ultra-portable business laptop featuring 14-inch 2.8K OLED display. Intel 13th Gen Core i7 processor with up to 16 hours battery life. Military-grade durability with premium carbon fiber construction.',
 1, 1, 10999.00, 14999.00, 189, 1456, 167, 245, '2024-03-16 11:30:00'),

-- Electronics - Audio (13)
(10007, 5001, 13, 'Sony', 'WH-1000XM5', 'Industry-leading noise canceling headphones', '/cdn/images/sonywh1000xm5.jpg',
 'Best-in-class noise canceling with dual noise sensor technology. Crystal clear hands-free calling with precise voice pickup. Up to 30 hours battery life with quick charging.',
 1, 1, 2299.00, 2799.00, 789, 4567, 656, 892, '2024-03-16 12:10:00'),

(10008, 5001, 13, 'Apple', 'AirPods Pro 2nd Gen', 'Adaptive Transparency and Personalized Spatial Audio', '/cdn/images/airpodspro2.jpg',
 'Next-level Active Noise Cancellation and Adaptive Transparency. Personalized Spatial Audio with dynamic head tracking. Touch control for media and calls.',
 1, 1, 1899.00, 1899.00, 923, 5234, 778, 1045, '2024-03-17 16:25:00'),

(10009, 5001, 13, 'Bose', 'QuietComfort Earbuds', 'True wireless earbuds with noise cancellation', '/cdn/images/boseqc.jpg',
 'World-class noise cancellation in a comfortable, secure fit. High-fidelity audio with customizable sound profiles. Weather and sweat resistant design.',
 1, 1, 1799.00, 1799.00, 456, 2890, 389, 567, '2024-03-19 09:15:00'),

-- Fashion - Women's Clothing (21)
(10010, 5002, 21, 'Zara', 'Wool Blend Coat', 'Classic wool blend coat with minimalist design', '/cdn/images/woolcoat.jpg',
 'Timeless wool blend coat featuring clean lines and sophisticated tailoring. Perfect for professional and casual wear. Available in multiple colors with premium lining.',
 1, 1, 899.00, 1299.00, 245, 1567, 189, 298, '2024-03-19 10:50:00'),

(10011, 5002, 21, 'H&M', 'Silk Midi Dress', 'Elegant silk dress perfect for special occasions', '/cdn/images/silkdress.jpg',
 'Luxurious silk midi dress with flattering A-line silhouette. Features adjustable straps and hidden back zipper. Ideal for evening events and dinner dates.',
 1, 1, 499.00, 799.00, 367, 2234, 298, 445, '2024-03-21 13:15:00'),

(10012, 5002, 21, 'Uniqlo', 'Cashmere Sweater', 'Premium cashmere knitwear in classic styles', '/cdn/images/cashmere.jpg',
 'Ultra-soft 100% cashmere sweater with timeless design. Machine washable for easy care. Available in essential colors to complement any wardrobe.',
 1, 1, 699.00, 999.00, 189, 1345, 156, 234, '2024-03-23 14:20:00'),

-- Fashion - Men's Clothing (22)
(10013, 5002, 22, 'Nike', 'Dri-FIT Running Shirt', 'Moisture-wicking athletic shirt for performance', '/cdn/images/nikerunshirt.jpg',
 'Nike Dri-FIT technology moves sweat away from your skin for quicker evaporation. Lightweight and breathable fabric with reflective elements for visibility.',
 1, 1, 299.00, 399.00, 678, 3456, 567, 789, '2024-03-22 11:40:00'),

(10014, 5002, 22, 'Ralph Lauren', 'Oxford Shirt', 'Classic button-down shirt in premium cotton', '/cdn/images/oxfordshirt.jpg',
 'Timeless oxford shirt crafted from 100% cotton with signature embroidered logo. Perfect for business casual and weekend wear. Machine washable.',
 1, 1, 599.00, 799.00, 234, 1678, 198, 289, '2024-03-24 15:30:00'),

-- Home & Living - Furniture (31)
(10015, 5004, 31, 'IKEA', 'Modern Sofa Set', 'Comfortable 3-seater sofa with modular design', '/cdn/images/modernsofaset.jpg',
 'Contemporary 3-seater sofa with high-quality foam cushions and durable fabric upholstery. Modular design allows for flexible arrangement in any living space.',
 1, 1, 2999.00, 4999.00, 89, 567, 67, 134, '2024-03-23 14:20:00'),

(10016, 5004, 31, 'West Elm', 'Dining Table Set', 'Solid wood dining table with 6 chairs', '/cdn/images/diningtable.jpg',
 'Handcrafted dining set made from sustainable solid wood. Seats 6 people comfortably. Includes matching chairs with upholstered cushions.',
 1, 1, 3999.00, 5999.00, 56, 445, 45, 78, '2024-03-25 10:45:00'),

-- Home & Living - Appliances (32)
(10017, 5004, 32, 'Dyson', 'V15 Detect Vacuum', 'Laser detect technology with powerful suction', '/cdn/images/dysonv15.jpg',
 'Revolutionary laser technology reveals microscopic dust. Powerful suction adapts automatically to different floor types. Real-time display shows particle count and size.',
 1, 1, 3999.00, 4499.00, 234, 1456, 198, 298, '2024-03-24 09:30:00'),

(10018, 5004, 32, 'KitchenAid', 'Stand Mixer', 'Professional stand mixer for baking enthusiasts', '/cdn/images/standmixer.jpg',
 '5.5-quart capacity stand mixer with 10 speeds. Includes dough hook, wire whip, and flat beater. Perfect for bread, cookies, and cake preparation.',
 1, 1, 2499.00, 2999.00, 167, 890, 134, 201, '2024-03-26 16:15:00'),

-- Books - Technology (41)
(10019, 5003, 41, 'O''Reilly', 'System Design Interview', 'Complete guide to system design interviews', '/cdn/images/systemdesign.jpg',
 'Comprehensive guide covering all aspects of system design interviews. Includes real-world examples, scalability patterns, and practical tips from industry experts.',
 1, 1, 199.00, 299.00, 1234, 6789, 892, 1345, '2024-03-25 15:45:00'),

(10020, 5003, 41, 'Manning', 'Clean Architecture', 'A craftsman''s guide to software structure and design', '/cdn/images/cleanarchitecture.jpg',
 'Essential principles of clean architecture from software legend Robert C. Martin. Learn to create maintainable, flexible, and testable software systems.',
 1, 1, 159.00, 229.00, 987, 5432, 698, 945, '2024-03-26 12:00:00'),

(10021, 5003, 41, 'Addison-Wesley', 'Effective Java', 'Best practices for Java programming language', '/cdn/images/effectivejava.jpg',
 'Essential Java programming guide by Joshua Bloch. Updated for Java 17 with best practices, design patterns, and performance optimization techniques.',
 1, 1, 179.00, 249.00, 756, 4234, 567, 823, '2024-03-27 13:30:00'),

-- Sports - Fitness Equipment (51)
(10022, 5005, 51, 'NordicTrack', 'Adjustable Dumbbells', 'Space-saving adjustable dumbbell set', '/cdn/images/adjustabledumbbells.jpg',
 'Revolutionary adjustable dumbbells that replace 15 sets of weights. Quick weight changes from 5 to 50 lbs per dumbbell. Compact design saves space.',
 1, 1, 1999.00, 2499.00, 145, 890, 123, 189, '2024-03-27 10:15:00'),

(10023, 5005, 51, 'Peloton', 'Yoga Mat', 'Premium yoga mat for all fitness levels', '/cdn/images/yogamat.jpg',
 'Professional-grade yoga mat with superior grip and cushioning. 6mm thickness provides optimal comfort and stability. Non-toxic and eco-friendly materials.',
 1, 1, 199.00, 199.00, 345, 1678, 278, 389, '2024-03-28 11:45:00'),

-- Beauty - Cosmetics (6)
(10024, 5008, 6, 'L''Oreal', 'Hydrating Face Serum', 'Anti-aging serum with hyaluronic acid', '/cdn/images/faceserum.jpg',
 'Advanced anti-aging serum with pure hyaluronic acid and vitamin C. Deeply hydrates skin and reduces fine lines. Suitable for all skin types.',
 1, 1, 299.00, 399.00, 456, 2345, 345, 478, '2024-03-28 16:30:00'),

(10025, 5008, 6, 'Estee Lauder', 'Night Repair Complex', 'Advanced night time skincare treatment', '/cdn/images/nightrepair.jpg',
 'Powerful overnight treatment that helps repair and protect skin while you sleep. Reduces visible signs of aging and improves skin texture.',
 1, 1, 899.00, 1199.00, 234, 1456, 189, 267, '2024-03-29 09:20:00'),

-- Food - Organic Products (7)
(10026, 5006, 7, 'Organic Valley', 'Protein Powder', 'Plant-based protein powder with superfoods', '/cdn/images/proteinpowder.jpg',
 'Certified organic plant-based protein powder with 25g protein per serving. Includes superfoods and essential amino acids. No artificial additives.',
 1, 1, 399.00, 499.00, 234, 1234, 167, 234, '2024-03-29 08:45:00'),

(10027, 5006, 7, 'Whole Foods', 'Organic Tea Collection', 'Premium organic tea variety pack', '/cdn/images/organictea.jpg',
 'Carefully curated collection of organic teas including green tea, black tea, and herbal blends. Sourced from certified organic farms worldwide.',
 1, 1, 149.00, 199.00, 345, 1789, 234, 345, '2024-03-30 07:30:00'),

-- Toys - Educational (8)
(10028, 5007, 8, 'LEGO', 'Architecture Set', 'Build famous landmarks with LEGO bricks', '/cdn/images/legoarchitecture.jpg',
 'Educational building set featuring famous architectural landmarks. Develops creativity, fine motor skills, and historical knowledge. Detailed instruction manual included.',
 1, 1, 599.00, 899.00, 178, 1098, 134, 201, '2024-03-30 13:20:00'),

(10029, 5007, 8, 'Melissa & Doug', 'Wooden Puzzle Set', 'Educational wooden puzzles for toddlers', '/cdn/images/woodenpuzzle.jpg',
 'High-quality wooden puzzles designed to develop problem-solving skills and hand-eye coordination. Non-toxic finishes and child-safe construction.',
 1, 1, 99.00, 149.00, 567, 2345, 456, 678, '2024-03-31 14:45:00'),

(10030, 5007, 8, 'Fisher-Price', 'Learning Tablet', 'Interactive tablet for early learning', '/cdn/images/learningtablet.jpg',
 'Interactive learning tablet with games, songs, and activities. Teaches letters, numbers, colors, and shapes. Durable design perfect for little hands.',
 1, 1, 299.00, 399.00, 234, 1456, 189, 267, '2024-04-01 16:00:00'),

(10031, 5003, 41, 'Prentice Hall', 'Clean Code (Chinese Edition)', 'Practical software craftsmanship guide with case-driven chapters', '/cdn/images/clean_code.jpg',
 'Covers naming standards, function granularity, error handling strategies, testability, and refactoring paths. Includes common anti-pattern discussions and readability-driven coding conventions. Paperback and hardcover versions are available for different reading preferences.',
 1, 1, 79.00, 129.00, 641, 4120, 313, 802, '2024-03-31 09:04:00'),

(10032, 5003, 43, 'Shambhala', 'The Art of War', 'Sun Tzu''s timeless classic on strategy', '/cdn/images/artofwar.jpg',
 'A foundational text on strategy, leadership, and decision-making. Suitable for readers interested in military history, business strategy, and classical literature.',
 1, 1, 12.00, 28.00, 580, 7202, 350, 600, '2024-03-31 09:05:00'),

(10033, 5003, 41, 'MIT Press', 'Introduction to Algorithms CLRS', 'The comprehensive guide to algorithms by Cormen et al.', '/cdn/images/intro_to_algo.jpg',
 'Comprehensive reference covering algorithm design, analysis, data structures, and advanced topics. Widely used in computer science education and interview preparation.',
 1, 1, 70.00, 95.00, 190, 3800, 140, 260, '2024-03-31 09:06:00'),

(10034, 5003, 41, 'Addison-Wesley', 'Design Patterns GoF', 'Elements of Reusable Object-Oriented Software', '/cdn/images/design_patterns_gof.jpg',
 'Classic reference introducing reusable object-oriented design patterns. Covers creational, structural, and behavioral patterns with practical examples for software architecture and maintainable design.',
 1, 1, 42.00, 65.00, 310, 4500, 200, 380, '2024-03-31 09:07:00');

INSERT INTO product_sku (
    id, spu_id, sku_code, sku_name, sale_price, origin_price, stock, lock_stock, warning_stock,
    image_url, spec_json, status, version
) VALUES
(20001, 10001, 'SKU-20001', 'iPhone 15 Pro Max 256GB Natural Titanium', 8999.00, 9999.00, 45, 0, 8, '/cdn/images/iphone15promax-256gb-natural-titanium.jpg', '{"storage":"256GB","color":"Natural Titanium"}', 1, 0),
(20002, 10001, 'SKU-20002', 'iPhone 15 Pro Max 512GB Natural Titanium', 10499.00, 11499.00, 32, 0, 6, '/cdn/images/iphone15promax-256gb-natural-titanium.jpg', '{"storage":"512GB","color":"Natural Titanium"}', 1, 0),
(20003, 10001, 'SKU-20003', 'iPhone 15 Pro Max 1TB Natural Titanium', 12999.00, 13999.00, 18, 0, 4, '/cdn/images/iphone15promax-256gb-natural-titanium.jpg', '{"storage":"1TB","color":"Natural Titanium"}', 1, 0),
(20004, 10001, 'SKU-20004', 'iPhone 15 Pro Max 256GB Blue Titanium', 8999.00, 9999.00, 38, 0, 8, '/cdn/images/iphone15promax-256gb-blue-titanium.jpg', '{"storage":"256GB","color":"Blue Titanium"}', 1, 0),
(20005, 10001, 'SKU-20005', 'iPhone 15 Pro Max 512GB Blue Titanium', 10499.00, 11499.00, 28, 0, 6, '/cdn/images/iphone15promax-256gb-blue-titanium.jpg', '{"storage":"512GB","color":"Blue Titanium"}', 1, 0),
(20006, 10001, 'SKU-20006', 'iPhone 15 Pro Max 256GB Black Titanium', 8999.00, 9999.00, 42, 0, 8, '/cdn/images/iphone15promax-256gb-black-titanium.jpg', '{"storage":"256GB","color":"Black Titanium"}', 1, 0),
(20007, 10002, 'SKU-20007', 'Galaxy S24 Ultra 256GB Titanium Black', 7999.00, 8999.00, 35, 0, 7, '/cdn/images/galaxys24ultra-256gb-titanium-black.jpg', '{"storage":"256GB","color":"Titanium Black"}', 1, 0),
(20008, 10002, 'SKU-20008', 'Galaxy S24 Ultra 512GB Titanium Black', 9499.00, 10499.00, 22, 0, 5, '/cdn/images/galaxys24ultra-256gb-titanium-black.jpg', '{"storage":"512GB","color":"Titanium Black"}', 1, 0),
(20009, 10002, 'SKU-20009', 'Galaxy S24 Ultra 1TB Titanium Black', 10999.00, 11999.00, 15, 0, 3, '/cdn/images/galaxys24ultra-256gb-titanium-black.jpg', '{"storage":"1TB","color":"Titanium Black"}', 1, 0),
(20010, 10002, 'SKU-20010', 'Galaxy S24 Ultra 256GB Titanium Gray', 7999.00, 8999.00, 30, 0, 7, '/cdn/images/galaxys24ultra-256gb-titanium-gray.jpg', '{"storage":"256GB","color":"Titanium Gray"}', 1, 0),
(20011, 10004, 'SKU-20011', 'MacBook Air M3 8GB/256GB Space Gray', 8999.00, 9999.00, 25, 0, 5, '/cdn/images/macbookairm3-8gb-256gb-space-gray.jpg', '{"memory":"8GB","storage":"256GB","color":"Space Gray"}', 1, 0),
(20012, 10004, 'SKU-20012', 'MacBook Air M3 16GB/512GB Space Gray', 11999.00, 12999.00, 18, 0, 4, '/cdn/images/macbookairm3-8gb-256gb-space-gray.jpg', '{"memory":"16GB","storage":"512GB","color":"Space Gray"}', 1, 0),
(20013, 10004, 'SKU-20013', 'MacBook Air M3 8GB/256GB Silver', 8999.00, 9999.00, 22, 0, 5, '/cdn/images/macbookairm3-8gb-256gb-silver.jpg', '{"memory":"8GB","storage":"256GB","color":"Silver"}', 1, 0),
(20014, 10004, 'SKU-20014', 'MacBook Air M3 16GB/512GB Silver', 11999.00, 12999.00, 16, 0, 4, '/cdn/images/macbookairm3-8gb-256gb-silver.jpg', '{"memory":"16GB","storage":"512GB","color":"Silver"}', 1, 0),
(20015, 10007, 'SKU-20015', 'Sony WH-1000XM5 Black', 2299.00, 2599.00, 65, 0, 12, '/cdn/images/sonywh1000xm5-black.jpg', '{"color":"Black"}', 1, 0),
(20016, 10007, 'SKU-20016', 'Sony WH-1000XM5 Silver', 2399.00, 2599.00, 58, 0, 12, '/cdn/images/sonywh1000xm5-silver.jpg', '{"color":"Silver"}', 1, 0),
(20017, 10010, 'SKU-20017', 'Wool Blend Coat M Black', 899.00, 999.00, 45, 0, 8, '/cdn/images/woolcoat-m-black.jpg', '{"size":"M","color":"Black"}', 1, 0),
(20018, 10010, 'SKU-20018', 'Wool Blend Coat L Black', 899.00, 999.00, 38, 0, 8, '/cdn/images/woolcoat-m-black.jpg', '{"size":"L","color":"Black"}', 1, 0),
(20019, 10010, 'SKU-20019', 'Wool Blend Coat M Navy', 1099.00, 1199.00, 42, 0, 8, '/cdn/images/woolcoat-m-navy.jpg', '{"size":"M","color":"Navy"}', 1, 0),
(20020, 10010, 'SKU-20020', 'Wool Blend Coat L Navy', 1099.00, 1199.00, 35, 0, 8, '/cdn/images/woolcoat-m-navy.jpg', '{"size":"L","color":"Navy"}', 1, 0),
(20021, 10019, 'SKU-20021', 'System Design Interview Paperback', 199.00, 249.00, 156, 0, 20, '/cdn/images/systemdesign.jpg', '{"format":"Paperback"}', 1, 0),
(20022, 10020, 'SKU-20022', 'Clean Architecture Hardcover', 229.00, 279.00, 124, 0, 15, '/cdn/images/cleanarchitecture.jpg', '{"format":"Hardcover"}', 1, 0),
(20023, 10021, 'SKU-20023', 'Effective Java 3rd Edition', 179.00, 229.00, 89, 0, 12, '/cdn/images/effectivejava.jpg', '{"edition":"3rd Edition"}', 1, 0),
(20024, 10015, 'SKU-20024', 'Modern Sofa Set Gray Fabric', 3499.00, 3999.00, 12, 0, 2, '/cdn/images/modernsofaset.jpg', '{"color":"Gray","material":"Fabric"}', 1, 0),
(20025, 10017, 'SKU-20025', 'Dyson V15 Detect Standard', 3999.00, 4299.00, 28, 0, 5, '/cdn/images/dysonv15.jpg', '{"model":"Standard"}', 1, 0),
(20026, 10022, 'SKU-20026', 'Adjustable Dumbbells 50lb Set', 1999.00, 2299.00, 35, 0, 8, '/cdn/images/adjustabledumbbells.jpg', '{"weight":"50lb"}', 1, 0),
(20027, 10011, 'SKU-20027', 'Silk Midi Dress M Navy', 499.00, 599.00, 72, 0, 12, '/cdn/images/silkdress-m-navy.jpg', '{"size":"M","color":"Navy"}', 1, 0),
(20028, 10027, 'SKU-20028', 'Organic Tea Collection', 149.00, 199.00, 48, 0, 10, '/cdn/images/organictea-variety-pack.jpg', '{"pack":"Variety"}', 1, 0),
(20029, 10028, 'SKU-20029', 'LEGO Architecture Set', 599.00, 899.00, 34, 0, 8, '/cdn/images/legoarchitecture-set.jpg', '{"theme":"Architecture"}', 1, 0),
(20030, 10024, 'SKU-20030', 'Hydrating Face Serum', 299.00, 399.00, 52, 0, 8, '/cdn/images/faceserum-hydrating.jpg', '{"type":"Serum"}', 1, 0),
(20031, 10025, 'SKU-20031', 'Night Repair Complex', 899.00, 1199.00, 41, 0, 8, '/cdn/images/nightrepair-complex.jpg', '{"type":"Night Care"}', 1, 0),
(20032, 10031, 'SKU-20032', 'Clean Code Chinese Edition Paperback', 79.00, 99.00, 160, 0, 15, '/cdn/images/clean_code.jpg', '{"format":"Paperback"}', 1, 0),
(20033, 10032, 'SKU-20033', 'The Art of War Paperback', 12.00, 28.00, 220, 0, 20, '/cdn/images/artofwar.jpg', '{"format":"Paperback"}', 1, 0),
(20034, 10033, 'SKU-20034', 'Introduction to Algorithms CLRS Hardcover', 70.00, 95.00, 160, 0, 15, '/cdn/images/intro_to_algo.jpg', '{"format":"Hardcover"}', 1, 0),
(20035, 10034, 'SKU-20035', 'Design Patterns GoF Paperback', 42.00, 65.00, 180, 0, 15, '/cdn/images/design_patterns_gof.jpg', '{"format":"Paperback"}', 1, 0);

INSERT INTO product_image (id, spu_id, sku_id, image_url, image_type, sort_order, create_time) VALUES
(9200, 10001, NULL, '/cdn/images/iphone15promax.jpg', 1, 0, '2026-03-31 09:00:00'),
(9201, 10002, NULL, '/cdn/images/galaxys24ultra.jpg', 1, 0, '2026-03-31 09:00:10'),
(9202, 10004, NULL, '/cdn/images/macbookairm3.jpg', 1, 0, '2026-03-31 09:01:00'),
(9203, 10007, NULL, '/cdn/images/sonywh1000xm5.jpg', 1, 0, '2026-03-31 09:02:00'),
(9204, 10011, NULL, '/cdn/images/silkdress.jpg', 1, 0, '2026-03-31 09:03:00'),
(9205, 10019, NULL, '/cdn/images/systemdesign.jpg', 1, 0, '2026-03-31 09:04:00'),
(9206, 10025, NULL, '/cdn/images/nightrepair.jpg', 1, 0, '2026-03-31 09:05:00'),
(9207, 10001, 20001, '/cdn/images/iphone15promax-256gb-natural-titanium.jpg', 3, 0, '2026-03-31 09:05:30'),
(9208, 10004, 20011, '/cdn/images/macbookairm3-8gb-256gb-space-gray.jpg', 3, 0, '2026-03-31 09:06:00'),
(9209, 10007, 20015, '/cdn/images/sonywh1000xm5-black.jpg', 3, 0, '2026-03-31 09:06:30'),
(9210, 10031, NULL, '/cdn/images/clean_code.jpg', 1, 0, '2026-03-31 09:04:00'),
(9211, 10031, 20032, '/cdn/images/clean_code.jpg', 3, 0, '2026-03-31 09:06:40'),
(9212, 10032, NULL, '/cdn/images/artofwar.jpg', 1, 0, '2026-03-31 09:05:00'),
(9213, 10032, 20033, '/cdn/images/artofwar.jpg', 3, 0, '2026-03-31 09:06:50'),
(9214, 10033, NULL, '/cdn/images/intro_to_algo.jpg', 1, 0, '2026-03-31 09:06:00'),
(9215, 10033, 20034, '/cdn/images/intro_to_algo.jpg', 3, 0, '2026-03-31 09:07:00'),
(9216, 10034, NULL, '/cdn/images/design_patterns_gof.jpg', 1, 0, '2026-03-31 09:07:10'),
(9217, 10034, 20035, '/cdn/images/design_patterns_gof.jpg', 3, 0, '2026-03-31 09:07:20');

INSERT INTO user_address (id, user_id, receiver_name, receiver_phone, province, city, district, detail_address, postal_code, is_default, tag_name) VALUES
(30001, 3001, 'Alice Chen', '13900003001', 'Beijing', 'Beijing', 'Haidian', 'No. 123 Zhongguancun Street, Building 5, Apt 1203', '100190', 1, 'Home'),
(30002, 3001, 'Alice Chen', '13900003001', 'Beijing', 'Beijing', 'Chaoyang', 'Tower A, Soho Modern City, Room 2105', '100020', 0, 'Office'),
(30003, 3002, 'Bob Wang', '13900003002', 'Shanghai', 'Shanghai', 'Pudong', 'No. 888 Century Avenue, Building 2, Floor 15', '200120', 1, 'Home'),
(30004, 3002, 'Wang Wei', '13900003002', 'Shanghai', 'Shanghai', 'Xuhui', 'No. 456 Huaihai Road, Apt 8B', '200031', 0, 'Parents Home'),
(30005, 3003, 'Carol Li', '13900003003', 'Guangdong', 'Shenzhen', 'Nanshan', 'No. 1001 Shennan Avenue, Coastal City, Tower 3, 2601', '518067', 1, 'Home'),
(30006, 3004, 'David Zhang', '13900003004', 'Zhejiang', 'Hangzhou', 'Xihu', 'No. 567 Wensan Road, West Lake Garden, Building A, 1502', '310013', 1, 'Home'),
(30007, 3004, 'David Zhang', '13900003004', 'Zhejiang', 'Hangzhou', 'Binjiang', 'No. 789 Jiangnan Avenue, Tech Park, Office 1001', '310051', 0, 'Office'),
(30008, 3005, 'Emily Liu', '13900003005', 'Jiangsu', 'Nanjing', 'Gulou', 'No. 234 Zhongshan Road, Golden Eagle, Tower B, 1808', '210008', 1, 'Home'),
(30009, 3006, 'Frank Wu', '13900003006', 'Sichuan', 'Chengdu', 'Jinjiang', 'No. 345 Chunxi Road, IFS Tower, Unit 2505', '610021', 1, 'Home'),
(30010, 3007, 'Grace Xu', '13900003007', 'Beijing', 'Beijing', 'Dongcheng', 'No. 678 Wangfujing Street, Oriental Plaza, Tower 1, 3201', '100006', 1, 'Home'),
(30011, 3007, 'Grace Xu', '13900003007', 'Beijing', 'Beijing', 'Haidian', 'Tsinghua University, Zijing Apartment, Building 12, 503', '100084', 0, 'School'),
(30012, 3008, 'Henry Zhao', '13900003008', 'Shanghai', 'Shanghai', 'Jingan', 'No. 890 Nanjing Road, Plaza 66, Tower 2, 4501', '200041', 1, 'Home'),
(30013, 3009, 'Ivy Sun', '13900003009', 'Guangdong', 'Guangzhou', 'Tianhe', 'No. 432 Tianhe Road, Citic Plaza, Tower A, 3505', '510620', 1, 'Home'),
(30014, 3010, 'Jack Ma', '13900003010', 'Zhejiang', 'Hangzhou', 'Binjiang', 'No. 969 Wenyi West Road, Alibaba Campus, Building 2, 801', '310052', 1, 'Office'),
(30015, 3010, 'Jack Ma', '13900003010', 'Zhejiang', 'Hangzhou', 'Xihu', 'No. 88 Xihu Avenue, West Lake Mansion, Villa 5', '310007', 0, 'Home'),
(30016, 3011, 'Kelly Huang', '13900003011', 'Jiangsu', 'Suzhou', 'Gusu', 'No. 555 Guanqian Street, Times Square, Tower B, 2201', '215002', 1, 'Home'),
(30017, 3012, 'Leo Tang', '13900003012', 'Sichuan', 'Chengdu', 'Wuhou', 'No. 777 Kehua Road, Raffles City, Tower 1, 1601', '610041', 1, 'Home');

INSERT INTO order_info (id, order_no, user_id, shop_id, total_amount, pay_amount, freight_amount, order_status, pay_status, receiver_name, receiver_phone, receiver_address, remark, create_time, update_time) VALUES
(40001, 'ORD20240315001', 3001, 5001, 8999.00, 9019.00, 20.00, 4, 1, 'Alice Chen', '13900003001', 'No. 123 Zhongguancun Street, Building 5, Apt 1203', '', '2024-03-15 14:30:00', '2024-03-18 10:15:00'),
(40002, 'ORD20240316002', 3002, 5002, 899.00, 919.00, 20.00, 4, 1, 'Bob Wang', '13900003002', 'No. 888 Century Avenue, Building 2, Floor 15', 'Please call before delivery', '2024-03-16 09:20:00', '2024-03-19 16:45:00'),
(40003, 'ORD20240317003', 3003, 5003, 398.00, 418.00, 20.00, 3, 1, 'Carol Li', '13900003003', 'No. 1001 Shennan Avenue, Coastal City, Tower 3, 2601', '', '2024-03-17 11:45:00', '2024-03-18 08:30:00'),
(40004, 'ORD20240318004', 3004, 5001, 2299.00, 2319.00, 20.00, 2, 1, 'David Zhang', '13900003004', 'No. 567 Wensan Road, West Lake Garden, Building A, 1502', '', '2024-03-18 15:20:00', '2024-03-18 15:25:00'),
(40005, 'ORD20240319005', 3005, 5004, 3999.00, 4029.00, 30.00, 4, 1, 'Emily Liu', '13900003005', 'No. 234 Zhongshan Road, Golden Eagle, Tower B, 1808', 'Fragile item', '2024-03-19 10:10:00', '2024-03-22 14:20:00'),
(40006, 'ORD20240320006', 3006, 5002, 1598.00, 1618.00, 20.00, 1, 0, 'Frank Wu', '13900003006', 'No. 345 Chunxi Road, IFS Tower, Unit 2505', '', '2024-03-20 16:30:00', '2024-03-20 16:30:00'),
(40007, 'ORD20240321007', 3007, 5005, 1999.00, 2019.00, 20.00, 4, 1, 'Grace Xu', '13900003007', 'No. 678 Wangfujing Street, Oriental Plaza, Tower 1, 3201', '', '2024-03-21 13:45:00', '2024-03-24 11:30:00'),
(40008, 'ORD20240322008', 3008, 5003, 588.00, 608.00, 20.00, 5, 1, 'Henry Zhao', '13900003008', 'No. 890 Nanjing Road, Plaza 66, Tower 2, 4501', 'Order cancelled by buyer', '2024-03-22 08:15:00', '2024-03-22 09:00:00'),
(40009, 'ORD20240323009', 3009, 5008, 699.00, 719.00, 20.00, 4, 1, 'Ivy Sun', '13900003009', 'No. 432 Tianhe Road, Citic Plaza, Tower A, 3505', '', '2024-03-23 12:00:00', '2024-03-26 09:45:00'),
(40010, 'ORD20240324010', 3010, 5001, 11999.00, 12019.00, 20.00, 2, 1, 'Jack Ma', '13900003010', 'No. 969 Wenyi West Road, Alibaba Campus, Building 2, 801', '', '2024-03-24 17:20:00', '2024-03-24 17:25:00'),
(40011, 'ORD20240325011', 3011, 5006, 498.00, 518.00, 20.00, 3, 1, 'Kelly Huang', '13900003011', 'No. 555 Guanqian Street, Times Square, Tower B, 2201', '', '2024-03-25 14:30:00', '2024-03-26 10:15:00'),
(40012, 'ORD20240326012', 3012, 5007, 898.00, 918.00, 20.00, 4, 1, 'Leo Tang', '13900003012', 'No. 777 Kehua Road, Raffles City, Tower 1, 1601', '', '2024-03-26 09:45:00', '2024-03-28 15:30:00'),
(40013, 'ORD20240327013', 3001, 5003, 457.00, 477.00, 20.00, 6, 2, 'Alice Chen', '13900003001', 'No. 123 Zhongguancun Street, Building 5, Apt 1203', 'Refund approved', '2024-03-27 11:20:00', '2024-03-29 16:45:00'),
(40014, 'ORD20240328014', 3003, 5005, 199.00, 219.00, 20.00, 4, 1, 'Carol Li', '13900003003', 'No. 1001 Shennan Avenue, Coastal City, Tower 3, 2601', '', '2024-03-28 13:15:00', '2024-03-30 12:20:00'),
(40015, 'ORD20240329015', 3005, 5001, 7999.00, 8019.00, 20.00, 2, 1, 'Emily Liu', '13900003005', 'No. 234 Zhongshan Road, Golden Eagle, Tower B, 1808', '', '2024-03-29 15:40:00', '2024-03-29 15:45:00');

INSERT INTO order_item (id, order_id, order_no, spu_id, sku_id, product_title, sku_name, sku_spec_json, product_image, sale_price, quantity, total_amount, review_status, create_time) VALUES
(41001, 40001, 'ORD20240315001', 10001, 20001, 'iPhone 15 Pro Max', 'iPhone 15 Pro Max 256GB Natural Titanium', '{"storage":"256GB","color":"Natural Titanium"}', '/cdn/images/iphone15promax-256gb-natural-titanium.jpg', 8999.00, 1, 8999.00, 1, '2024-03-15 14:30:00'),
(41002, 40002, 'ORD20240316002', 10010, 20017, 'Wool Blend Coat', 'Wool Blend Coat M Black', '{"size":"M","color":"Black"}', '/cdn/images/woolcoat-m-black.jpg', 899.00, 1, 899.00, 1, '2024-03-16 09:20:00'),
(41003, 40003, 'ORD20240317003', 10021, 20023, 'Effective Java', 'Effective Java 3rd Edition', '{"edition":"3rd Edition"}', '/cdn/images/effectivejava.jpg', 179.00, 1, 179.00, 1, '2024-03-17 11:45:00'),
(41004, 40003, 'ORD20240317003', 10020, 20022, 'Clean Architecture', 'Clean Architecture Hardcover', '{"format":"Hardcover"}', '/cdn/images/cleanarchitecture.jpg', 229.00, 1, 229.00, 1, '2024-03-17 11:45:00'),
(41005, 40004, 'ORD20240318004', 10007, 20015, 'WH-1000XM5', 'Sony WH-1000XM5 Black', '{"color":"Black"}', '/cdn/images/sonywh1000xm5-black.jpg', 2299.00, 1, 2299.00, 1, '2024-03-18 15:20:00'),
(41006, 40005, 'ORD20240319005', 10017, 20025, 'V15 Detect Vacuum', 'Dyson V15 Detect Standard', '{"model":"Standard"}', '/cdn/images/dysonv15.jpg', 3999.00, 1, 3999.00, 1, '2024-03-19 10:10:00'),
(41007, 40006, 'ORD20240320006', 10010, 20019, 'Wool Blend Coat', 'Wool Blend Coat M Navy', '{"size":"M","color":"Navy"}', '/cdn/images/woolcoat-m-navy.jpg', 1099.00, 1, 1099.00, 0, '2024-03-20 16:30:00'),
(41008, 40006, 'ORD20240320006', 10011, 20027, 'Silk Midi Dress', 'Silk Midi Dress M Navy', '{"size":"M","color":"Navy"}', '/cdn/images/silkdress.jpg', 499.00, 1, 499.00, 0, '2024-03-20 16:30:00'),
(41009, 40007, 'ORD20240321007', 10022, 20026, 'Adjustable Dumbbells', 'Adjustable Dumbbells 50lb Set', '{"weight":"50lb"}', '/cdn/images/adjustabledumbbells.jpg', 1999.00, 1, 1999.00, 1, '2024-03-21 13:45:00'),
(41010, 40008, 'ORD20240322008', 10019, 20021, 'System Design Interview', 'System Design Interview Paperback', '{"format":"Paperback"}', '/cdn/images/systemdesign.jpg', 199.00, 1, 199.00, 0, '2024-03-22 08:15:00'),
(41011, 40008, 'ORD20240322008', 10020, 20022, 'Clean Architecture', 'Clean Architecture Hardcover', '{"format":"Hardcover"}', '/cdn/images/cleanarchitecture.jpg', 229.00, 1, 229.00, 0, '2024-03-22 08:15:00'),
(41012, 40009, 'ORD20240323009', 10024, 20030, 'Hydrating Face Serum', 'Hydrating Face Serum', '{"type":"Serum"}', '/cdn/images/faceserum.jpg', 299.00, 1, 299.00, 1, '2024-03-23 12:00:00'),
(41013, 40009, 'ORD20240323009', 10025, 20031, 'Night Repair Complex', 'Night Repair Complex', '{"type":"Night Care"}', '/cdn/images/nightrepair.jpg', 899.00, 1, 899.00, 1, '2024-03-23 12:00:00'),
(41014, 40010, 'ORD20240324010', 10004, 20012, 'MacBook Air M3', 'MacBook Air M3 16GB/512GB Space Gray', '{"memory":"16GB","storage":"512GB","color":"Space Gray"}', '/cdn/images/macbookairm3-8gb-256gb-space-gray.jpg', 11999.00, 1, 11999.00, 1, '2024-03-24 17:20:00'),
(41015, 40011, 'ORD20240325011', 10026, 20026, 'Protein Powder', 'Adjustable Dumbbells 50lb Set', '{"weight":"50lb"}', '/cdn/images/proteinpowder.jpg', 399.00, 1, 399.00, 1, '2024-03-25 14:30:00');

INSERT INTO product_review (id, order_item_id, order_no, user_id, spu_id, sku_id, score, content, anonymous_flag, review_status, create_time) VALUES
(50001, 9801, 'ORD-20260330-001', 3001, 10001, 20001, 5, 'Camera quality is outstanding and battery easily lasts a full day.', 0, 1, '2024-03-18 20:15:00'),
(50002, 9802, 'ORD-20260330-002', 3005, 10001, 20004, 5, 'Premium feel and excellent photography performance.', 0, 1, '2024-03-20 14:30:00'),
(50003, 9803, 'ORD-20260330-003', 3007, 10001, 20003, 4, 'Fast and smooth, price is high but the experience is strong.', 0, 1, '2024-03-22 11:20:00'),
(50004, 9804, 'ORD-20260330-004', 3009, 10001, 20001, 5, 'Great for content creation and video work.', 0, 1, '2024-03-25 16:45:00'),
(50005, 9805, 'ORD-20260330-005', 3011, 10001, 20004, 4, 'Solid upgrade and very comfortable to use.', 0, 1, '2024-03-28 09:30:00'),
(50006, 9806, 'ORD-20260330-006', 3002, 10002, 20007, 5, 'Galaxy AI features are genuinely helpful.', 0, 1, '2024-03-19 13:15:00'),
(50007, 9807, 'ORD-20260330-007', 3004, 10002, 20008, 4, 'Excellent display and useful S Pen support.', 0, 1, '2024-03-21 18:20:00'),
(50008, 9808, 'ORD-20260330-008', 3006, 10002, 20009, 5, 'Best Android phone I have used so far.', 0, 1, '2024-03-24 10:45:00'),
(50009, 9809, 'ORD-20260330-009', 3008, 10002, 20010, 3, 'Hardware is solid but the software feels busy.', 0, 1, '2024-03-26 15:30:00'),
(50010, 9810, 'ORD-20260330-010', 3003, 10004, 20011, 5, 'Great battery life and very quiet operation.', 0, 1, '2024-03-17 12:40:00'),
(50011, 9811, 'ORD-20260330-011', 3010, 10004, 20012, 5, 'Fast, light, and ideal for everyday work.', 0, 1, '2024-03-25 11:20:00'),
(50012, 9812, 'ORD-20260330-012', 3012, 10004, 20013, 4, 'Lightweight and great for travel.', 0, 1, '2024-03-29 14:15:00'),
(50013, 9813, 'ORD-20260330-013', 3004, 10007, 20015, 5, 'Best noise canceling headphones for commute and work.', 0, 1, '2024-03-19 19:25:00'),
(50014, 9814, 'ORD-20260330-014', 3006, 10007, 20016, 4, 'Comfortable and long lasting battery.', 0, 1, '2024-03-23 16:50:00'),
(50015, 9815, 'ORD-20260330-015', 3008, 10007, 20015, 5, 'Excellent for travel and conference calls.', 0, 1, '2024-03-27 08:30:00'),
(50016, 9816, 'ORD-20260330-016', 3010, 10007, 20016, 3, 'Good but not dramatically better than the previous generation.', 0, 1, '2024-03-30 13:45:00'),
(50017, 9817, 'ORD-20260330-017', 3002, 10010, 20017, 4, 'Beautiful coat with excellent tailoring.', 0, 1, '2024-03-19 21:10:00'),
(50018, 9818, 'ORD-20260330-018', 3006, 10010, 20019, 5, 'Navy color is accurate and quality is great.', 0, 1, '2024-03-25 10:20:00'),
(50019, 9819, 'ORD-20260330-019', 3001, 10011, 20018, 4, 'Elegant dress with a flattering cut.', 0, 1, '2024-03-21 14:35:00'),
(50020, 9820, 'ORD-20260330-020', 3003, 10019, 20021, 5, 'Excellent resource for system design interviews.', 0, 1, '2024-03-18 22:15:00'),
(50021, 9821, 'ORD-20260330-021', 3003, 10020, 20022, 5, 'Clear examples and practical advice.', 0, 1, '2024-03-20 16:40:00'),
(50022, 9822, 'ORD-20260330-022', 3008, 10021, 20023, 4, 'Comprehensive guide with practical examples.', 0, 1, '2024-03-23 11:25:00'),
(50023, 9823, 'ORD-20260330-023', 3005, 10015, 20024, 4, 'Good quality sofa and straightforward assembly.', 0, 1, '2024-03-23 19:30:00'),
(50024, 9824, 'ORD-20260330-024', 3005, 10017, 20025, 5, 'Laser dust detection is surprisingly useful.', 0, 1, '2024-03-25 12:45:00'),
(50025, 9825, 'ORD-20260330-025', 3007, 10022, 20026, 5, 'Perfect for home gym setup.', 0, 1, '2024-03-26 08:15:00'),
(50026, 9826, 'ORD-20260330-026', 3009, 10024, 20024, 4, 'Skin feels more hydrated after a few weeks.', 0, 1, '2024-03-28 17:20:00'),
(50027, 9827, 'ORD-20260330-027', 3009, 10025, 20025, 5, 'Night treatment works well and lasts a long time.', 0, 1, '2024-03-29 20:50:00'),
(50028, 9828, 'ORD-20260330-028', 3012, 10001, 20001, 2, 'Battery life did not meet expectations.', 0, 1, '2024-03-30 14:25:00'),
(50029, 9829, 'ORD-20260330-029', 3001, 10002, 20007, 3, 'Hardware is good but the interface can feel crowded.', 0, 1, '2024-03-31 09:40:00'),
(50030, 9830, 'ORD-20260330-030', 3011, 10019, 20021, 5, 'Excellent value for career preparation.', 0, 1, '2024-04-01 16:30:00');

INSERT INTO user_browse_history (id, user_id, spu_id, browse_time) VALUES
(60001, 3001, 10001, '2024-03-14 10:15:00'),
(60002, 3001, 10001, '2024-03-14 10:30:00'),
(60003, 3001, 10002, '2024-03-14 10:45:00'),
(60004, 3001, 10004, '2024-03-14 11:20:00'),
(60005, 3001, 10007, '2024-03-14 14:30:00'),
(60006, 3001, 10001, '2024-03-15 09:45:00'),
(60007, 3001, 10005, '2024-03-16 16:20:00'),
(60008, 3001, 10019, '2024-03-17 11:30:00'),
(60009, 3001, 10020, '2024-03-17 11:35:00'),
(60010, 3001, 10021, '2024-03-17 11:40:00'),
(60011, 3002, 10010, '2024-03-15 14:20:00'),
(60012, 3002, 10011, '2024-03-15 14:25:00'),
(60013, 3002, 10012, '2024-03-15 14:30:00'),
(60014, 3002, 10010, '2024-03-15 20:15:00'),
(60015, 3002, 10010, '2024-03-16 08:45:00'),
(60016, 3002, 10013, '2024-03-16 12:30:00'),
(60017, 3002, 10014, '2024-03-17 09:15:00'),
(60018, 3003, 10019, '2024-03-16 19:30:00'),
(60019, 3003, 10020, '2024-03-16 19:35:00'),
(60020, 3003, 10021, '2024-03-16 19:40:00'),
(60021, 3003, 10019, '2024-03-17 10:20:00'),
(60022, 3003, 10020, '2024-03-17 10:25:00'),
(60023, 3003, 10004, '2024-03-18 15:45:00'),
(60024, 3003, 10001, '2024-03-19 11:20:00'),
(60025, 3004, 10007, '2024-03-17 09:30:00'),
(60026, 3004, 10002, '2024-03-17 13:45:00'),
(60027, 3004, 10007, '2024-03-18 08:20:00'),
(60028, 3004, 10007, '2024-03-18 14:50:00'),
(60029, 3004, 10004, '2024-03-19 16:30:00'),
(60030, 3004, 10005, '2024-03-19 16:35:00'),
(60031, 3004, 10006, '2024-03-19 16:40:00'),
(60032, 3005, 10015, '2024-03-18 11:20:00'),
(60033, 3005, 10016, '2024-03-18 11:30:00'),
(60034, 3005, 10017, '2024-03-18 14:45:00'),
(60035, 3005, 10018, '2024-03-18 14:50:00'),
(60036, 3005, 10017, '2024-03-19 09:30:00'),
(60037, 3005, 10002, '2024-03-20 20:15:00'),
(60038, 3005, 10001, '2024-03-20 20:20:00'),
(60039, 3006, 10010, '2024-03-19 12:15:00'),
(60040, 3006, 10011, '2024-03-19 12:20:00'),
(60041, 3007, 10022, '2024-03-20 18:30:00'),
(60042, 3007, 10023, '2024-03-20 18:35:00'),
(60043, 3008, 10019, '2024-03-21 21:45:00'),
(60044, 3009, 10024, '2024-03-22 13:20:00'),
(60045, 3009, 10025, '2024-03-22 13:25:00'),
(60046, 3010, 10004, '2024-03-23 10:40:00'),
(60047, 3011, 10026, '2024-03-24 14:55:00'),
(60048, 3012, 10028, '2024-03-25 16:10:00');

INSERT INTO user_favorite (id, user_id, spu_id, create_time) VALUES
(65001, 3001, 10004, '2024-03-14 11:25:00'),
(65002, 3001, 10007, '2024-03-14 14:35:00'),
(65003, 3001, 10020, '2024-03-17 11:45:00'),
(65004, 3002, 10011, '2024-03-15 14:35:00'),
(65005, 3002, 10013, '2024-03-16 12:35:00'),
(65006, 3002, 10014, '2024-03-17 09:20:00'),
(65007, 3003, 10021, '2024-03-16 19:45:00'),
(65008, 3003, 10001, '2024-03-19 11:25:00'),
(65009, 3003, 10004, '2024-03-18 15:50:00'),
(65010, 3004, 10005, '2024-03-19 16:45:00'),
(65011, 3004, 10006, '2024-03-19 16:50:00'),
(65012, 3005, 10016, '2024-03-18 11:40:00'),
(65013, 3005, 10018, '2024-03-18 14:55:00'),
(65014, 3005, 10001, '2024-03-20 20:30:00'),
(65015, 3006, 10012, '2024-03-19 12:30:00'),
(65016, 3007, 10023, '2024-03-20 18:40:00'),
(65017, 3008, 10020, '2024-03-21 21:50:00'),
(65018, 3009, 10026, '2024-03-22 13:40:00'),
(65019, 3010, 10005, '2024-03-23 10:45:00'),
(65020, 3011, 10027, '2024-03-24 15:00:00'),
(65021, 3012, 10029, '2024-03-25 16:15:00');

INSERT INTO cart_item (id, user_id, sku_id, quantity, checked) VALUES
(68001, 3001, 20012, 1, 1),
(68002, 3001, 20016, 1, 1),
(68003, 3002, 20014, 1, 1),
(68004, 3002, 20018, 1, 1),
(68005, 3003, 20001, 1, 1),
(68006, 3003, 20013, 1, 1),
(68007, 3004, 20002, 1, 1),
(68008, 3004, 20015, 1, 1),
(68009, 3005, 20007, 1, 1),
(68010, 3005, 20024, 1, 1),
(68011, 3006, 20019, 1, 1),
(68012, 3007, 20026, 1, 1),
(68013, 3008, 20023, 2, 1),
(68014, 3009, 20024, 1, 1),
(68015, 3010, 20005, 1, 1),
(68016, 3011, 20028, 2, 1),
(68017, 3012, 20029, 1, 1);

INSERT INTO payment_record (id, order_no, pay_no, user_id, pay_amount, pay_method, pay_status, third_trade_no, pay_time, create_time) VALUES
(45001, 'ORD20240315001', 'PAY20240315001', 3001, 9019.00, 1, 1, 'ALI2024031500001234', '2024-03-15 14:35:00', '2024-03-15 14:35:00'),
(45002, 'ORD20240316002', 'PAY20240316002', 3002, 919.00, 1, 1, 'WX20240316001234567', '2024-03-16 09:25:00', '2024-03-16 09:25:00'),
(45003, 'ORD20240317003', 'PAY20240317003', 3003, 418.00, 1, 1, 'ALI2024031700002345', '2024-03-17 11:50:00', '2024-03-17 11:50:00'),
(45004, 'ORD20240318004', 'PAY20240318004', 3004, 2319.00, 1, 1, 'BANK20240318003456', '2024-03-18 15:22:00', '2024-03-18 15:22:00'),
(45005, 'ORD20240319005', 'PAY20240319005', 3005, 4029.00, 1, 1, 'ALI2024031900003456', '2024-03-19 10:15:00', '2024-03-19 10:15:00'),
(45006, 'ORD20240321007', 'PAY20240321007', 3007, 2019.00, 1, 1, 'WX20240321002345678', '2024-03-21 13:50:00', '2024-03-21 13:50:00'),
(45007, 'ORD20240322008', 'PAY20240322008', 3008, 608.00, 1, 1, 'ALI2024032200004567', '2024-03-22 08:18:00', '2024-03-22 08:18:00'),
(45008, 'ORD20240323009', 'PAY20240323009', 3009, 719.00, 1, 1, 'WX20240323003456789', '2024-03-23 12:05:00', '2024-03-23 12:05:00'),
(45009, 'ORD20240324010', 'PAY20240324010', 3010, 12019.00, 1, 1, 'BANK20240324004567', '2024-03-24 17:22:00', '2024-03-24 17:22:00'),
(45010, 'ORD20240325011', 'PAY20240325011', 3011, 518.00, 1, 1, 'ALI2024032500005678', '2024-03-25 14:35:00', '2024-03-25 14:35:00'),
(45011, 'ORD20240326012', 'PAY20240326012', 3012, 918.00, 1, 1, 'WX20240326004567890', '2024-03-26 09:50:00', '2024-03-26 09:50:00'),
(45012, 'ORD20240327013', 'PAY20240327013', 3001, 477.00, 1, 1, 'ALI2024032700006789', '2024-03-27 11:22:00', '2024-03-27 11:22:00'),
(45013, 'ORD20240328014', 'PAY20240328014', 3003, 219.00, 1, 1, 'WX20240328005678901', '2024-03-28 13:18:00', '2024-03-28 13:18:00'),
(45014, 'ORD20240329015', 'PAY20240329015', 3005, 8019.00, 1, 1, 'BANK20240329005678', '2024-03-29 15:42:00', '2024-03-29 15:42:00');

INSERT INTO inventory_log (id, sku_id, order_no, change_count, before_stock, after_stock, operate_type, remark, create_time) VALUES
(70001, 20001, NULL, -1, 46, 45, 'OUT', 'Order Sale', '2024-03-15 14:30:00'),
(70002, 20017, NULL, -1, 46, 45, 'OUT', 'Order Sale', '2024-03-16 09:20:00'),
(70003, 20023, NULL, -1, 90, 89, 'OUT', 'Order Sale', '2024-03-17 11:45:00'),
(70004, 20022, NULL, -1, 125, 124, 'OUT', 'Order Sale', '2024-03-17 11:45:00'),
(70005, 20015, NULL, -1, 66, 65, 'OUT', 'Order Sale', '2024-03-18 15:20:00'),
(70006, 20025, NULL, -1, 29, 28, 'OUT', 'Order Sale', '2024-03-19 10:10:00'),
(70007, 20001, NULL, 20, 45, 65, 'IN', 'Supplier Restock', '2024-03-20 09:00:00'),
(70008, 20007, NULL, 15, 35, 50, 'IN', 'Supplier Restock', '2024-03-20 09:30:00'),
(70009, 20015, NULL, 25, 65, 90, 'IN', 'Supplier Restock', '2024-03-20 10:00:00'),
(70010, 20023, NULL, 1, 89, 90, 'IN', 'Customer Return', '2024-03-22 14:20:00'),
(70011, 20022, NULL, 1, 124, 125, 'IN', 'Customer Return', '2024-03-22 14:20:00'),
(70012, 20026, NULL, -1, 36, 35, 'OUT', 'Order Sale', '2024-03-21 13:45:00'),
(70013, 20021, NULL, -1, 157, 156, 'OUT', 'Order Sale', '2024-03-22 08:15:00'),
(70014, 20022, NULL, -1, 125, 124, 'OUT', 'Order Sale', '2024-03-22 08:15:00'),
(70015, 20001, NULL, -2, 65, 63, 'ADJUST', 'Quality Issue', '2024-03-23 11:30:00'),
(70016, 20007, NULL, -1, 50, 49, 'ADJUST', 'Damage Check', '2024-03-23 11:45:00'),
(70017, 20001, NULL, -5, 63, 58, 'RESERVE', 'Promotion Reserve', '2024-03-25 10:00:00'),
(70018, 20007, NULL, -3, 49, 46, 'RESERVE', 'Promotion Reserve', '2024-03-25 10:15:00'),
(70019, 20015, NULL, -8, 90, 82, 'RESERVE', 'Promotion Reserve', '2024-03-25 10:30:00'),
(70020, 20012, NULL, 2, 16, 18, 'ADJUST', 'Inventory Reconciliation', '2024-03-31 16:00:00'),
(70021, 20024, NULL, -1, 13, 12, 'ADJUST', 'Inventory Reconciliation', '2024-03-31 16:15:00'),
(70022, 20025, NULL, 3, 25, 28, 'ADJUST', 'Inventory Reconciliation', '2024-03-31 16:30:00');





