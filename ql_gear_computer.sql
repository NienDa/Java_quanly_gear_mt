-- CƠ SỞ DỮ LIỆU: ql_gear_computer
CREATE DATABASE IF NOT EXISTS ql_gear_computer;
USE ql_gear_computer;

-- Xóa các bảng cũ nếu tồn tại (theo thứ tự khóa ngoại để tránh lỗi)
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS inventories;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;


-- Bảng người dùng
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    email VARCHAR(150),
    phone VARCHAR(20),
    role VARCHAR(20)
);

-- Bảng danh mục sản phẩm
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Bảng sản phẩm
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    description TEXT,
    price DOUBLE,
    image_url VARCHAR(255),
    status VARCHAR(30),
    category_id BIGINT,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT chk_price CHECK (price > 0)
);

-- Bảng tồn kho
CREATE TABLE inventories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quantity INT DEFAULT 0,
    import_date DATE,
    product_id BIGINT UNIQUE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_quantity CHECK (quantity >= 0)
);

-- Bảng đơn hàng
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_date DATETIME,
    total_price DOUBLE DEFAULT 0,
    status VARCHAR(30),
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Bảng chi tiết đơn hàng
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quantity INT,
    unit_price DOUBLE,
    subtotal DOUBLE,
    order_id BIGINT,
    product_id BIGINT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- Bảng chi tiết giỏ hàng (MỚI)
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quantity INT DEFAULT 1,
    user_id BIGINT,
    product_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_product (user_id, product_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0)
);

-- Bảng thanh toán đơn hàng (MỚI)
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_date DATETIME,
    payment_method VARCHAR(50), 
    amount DOUBLE,
    status VARCHAR(30), 
    order_id BIGINT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);


-- 2. TẠO CÁC TRIGGERS
DELIMITER $$

-- Trigger 1: Kiểm tra tồn kho trước khi đặt hàng (nếu không đủ ném lỗi)
CREATE TRIGGER trg_check_inventory
BEFORE INSERT ON order_items
FOR EACH ROW
BEGIN
    DECLARE current_stock INT;
    
    SELECT quantity INTO current_stock
    FROM inventories
    WHERE product_id = NEW.product_id;
    
    IF current_stock IS NULL OR current_stock < NEW.quantity THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Not enough stock';
    END IF;
END $$

-- Trigger 2: Cập nhật giảm số lượng trong kho sau khi thêm chi tiết đơn hàng
CREATE TRIGGER trg_reduce_inventory
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    UPDATE inventories
    SET quantity = quantity - NEW.quantity
    WHERE product_id = NEW.product_id;
END $$

-- Trigger 3 (MỚI): Tự động tính toán subtotal (thành tiền) của order_item trước khi lưu
CREATE TRIGGER trg_calculate_subtotal
BEFORE INSERT ON order_items
FOR EACH ROW
BEGIN
    SET NEW.subtotal = NEW.quantity * NEW.unit_price;
END $$

-- Trigger 4 (MỚI): Tự động cập nhật tổng giá trị đơn hàng (total_price) trong bảng orders
CREATE TRIGGER trg_update_order_total
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    UPDATE orders
    SET total_price = (SELECT SUM(subtotal) FROM order_items WHERE order_id = NEW.order_id)
    WHERE id = NEW.order_id;
END $$

DELIMITER ;

-- 3. TẠO CÁC FUNCTIONS
DELIMITER $$

-- Function 1: Tính thành tiền (Phép nhân đơn giản)
CREATE FUNCTION CalculateSubtotal(qty INT, price DOUBLE)
RETURNS DOUBLE
DETERMINISTIC
BEGIN
    RETURN qty * price;
END $$

-- Function 2 (MỚI): Lấy số lượng hàng còn tồn kho của một sản phẩm
CREATE FUNCTION GetStock(p_product_id BIGINT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE stock_qty INT DEFAULT 0;
    SELECT quantity INTO stock_qty FROM inventories WHERE product_id = p_product_id;
    RETURN IFNULL(stock_qty, 0);
END $$

DELIMITER ;

-- 4. TẠO CÁC PROCEDURES
DELIMITER $$

-- Procedure 1: Lấy danh sách toàn bộ sản phẩm
CREATE PROCEDURE GetAllProducts()
BEGIN
    SELECT * FROM products;
END $$

-- Procedure 2: Tính tổng doanh thu từ các đơn hàng thành công (COMPLETED)
CREATE PROCEDURE CalculateRevenue()
BEGIN
    SELECT SUM(total_price) AS total_revenue
    FROM orders
    WHERE status = 'COMPLETED';
END $$

DELIMITER ;


-- 5. TẠO CÁC VIEWS
-- View 1: Xem chi tiết thông tin sản phẩm và số lượng tồn kho
CREATE VIEW product_detail_view AS
SELECT
    p.id AS product_id,
    p.product_name,
    p.brand,
    p.price,
    c.category_name,
    i.quantity AS stock_quantity
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
LEFT JOIN inventories i ON p.id = i.product_id;

-- View 2 (MỚI): Xem chi tiết đơn hàng kèm thông tin thanh toán của khách hàng
CREATE VIEW order_detail_view AS
SELECT 
    o.id AS order_id,
    o.order_date,
    u.full_name AS customer_name,
    o.total_price,
    o.status AS order_status,
    pay.payment_method,
    pay.status AS payment_status
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
LEFT JOIN payments pay ON o.id = pay.order_id;


-- 6. CHÈN DỮ LIỆU MẪU 
-- Chèn dữ liệu người dùng
INSERT INTO users(username, password, full_name, email, phone, role) VALUES
('admin', '123', 'Administrator', 'admin@gmail.com', '0900000001', 'ADMIN'),
('staff01', '123', 'Nguyen Van A', 'staff01@gmail.com', '0900000002', 'STAFF'),
('customer01', '123', 'Tran Van B', 'customer01@gmail.com', '0900000003', 'CUSTOMER'),
('customer02', '123', 'Le Thi C', 'customer02@gmail.com', '0900000004', 'CUSTOMER');

-- Chèn dữ liệu danh mục
INSERT INTO categories(category_name, description) VALUES
('Gaming Mouse', 'Chuột chơi game chính hãng'),
('Keyboard', 'Bàn phím cơ học chuyên dụng'),
('Headphone', 'Tai nghe chụp tai gaming có mic'),
('Monitor', 'Màn hình máy tính độ phân giải cao');

-- Chèn dữ liệu sản phẩm
INSERT INTO products(product_name, brand, description, price, image_url, status, category_id) VALUES
('Logitech G102', 'Logitech', 'Gaming mouse RGB siêu nhạy', 450000, 'g102.jpg', 'AVAILABLE', 1),
('Razer Viper Mini', 'Razer', 'Lightweight gaming mouse siêu nhẹ', 790000, 'viper-mini.jpg', 'AVAILABLE', 1),
('AKKO 3084', 'AKKO', 'Bàn phím cơ Akko 84 phím', 1500000, 'akko3084.jpg', 'AVAILABLE', 2),
('HyperX Cloud II', 'HyperX', 'Tai nghe HyperX âm thanh giả lập 7.1', 1990000, 'cloud2.jpg', 'AVAILABLE', 3),
('LG UltraGear 24', 'LG', 'Màn hình LG 24 inch 144Hz', 4500000, 'lgultragear.jpg', 'AVAILABLE', 4);

-- Chèn dữ liệu tồn kho sản phẩm tương ứng
INSERT INTO inventories(quantity, import_date, product_id) VALUES
(20, '2026-05-01', 1),
(15, '2026-05-01', 2),
(10, '2026-05-01', 3),
(8, '2026-05-01', 4),
(5, '2026-05-01', 5);
