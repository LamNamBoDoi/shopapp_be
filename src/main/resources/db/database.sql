DELETE FROM products;
ALTER TABLE ten_bang AUTO_INCREMENT = 1;
Câu lệnh này thiết lập giá trị tự động tăng tiếp theo
 (giá trị bắt đầu của cột thường là id) thành 1.
CREATE DATABASE shopapp;
USE shopapp;
-- Khách hàng khi muốn mua hàng => phải đăng ký tài khoản => bảng users
CREATE TABLE users(
    id INT PRIMARY KEY AUTO_INCREMENT,
    fullname VARCHAR(100) DEFAULT '',
    phone_number VARCHAR(10) NOT NULL,
    address VARCHAR(200) DEFAULT '',
    password VARCHAR(100) NOT NULL DEFAULT '',
    created_at DATETIME,
    updated_at DATETIME,
    is_active TINYINT(1) DEFAULT 1,
    date_of_birth DATE,
    facebook_account_id INT DEFAULT 0,
    google_account_id INT DEFAULT 0
);

ALTER TABLE users ADD COLUMN role_id INT;

CREATE TABLE roles(
    id INT PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

ALTER TABLE users ADD FOREIGN KEY (role_id) REFERENCES roles(id);

CREATE TABLE tokens(
    id INT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) UNIQUE NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    expiration_date DATETIME,
    revoked TINYINT(1) NOT NULL,
    expired TINYINT(1) NOT NULL,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- hỗ trợ đăng nhập từ facebook và google
CREATE TABLE social_account(
    id INT PRIMARY KEY AUTO_INCREMENT,
    provider VARCHAR(20) NOT NULL COMMENT 'Tên nhà social network',
    provider_id VARCHAR(50) NOT NULL,
    email VARCHAR(150) NOT NULL COMMENT 'Email tài khoản',
    name VARCHAR(100) NOT NULL COMMENT 'Tên người dùng',
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- bảng danh mục sản phẩm(category)
CREATE TABLE categories(
     id INT PRIMARY KEY AUTO_INCREMENT,
     name VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'Tên danh mục, vd: đồ điện tử'
);

-- bảng chứa sản phẩm (Product): "laptop macbook air 15 inch 2023",
CREATE TABLE products(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(350) COMMENT 'Tên sản phẩm',
    price FLOAT NOT NULL CHECK(price >= 0),
    thumbnail VARCHAR(300) DEFAULT '',
    description LONGTEXT DEFAULT '',
    created_at DATETIME,
    updated_at DATETIME,
    category_id INT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE product_images(
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_images_product_id
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(300)
);

-- đặt hàng - orders
CREATE TABLE orders(
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id int,
    FOREIGN KEY (user_id) REFERENCES users(id),
    fullname VARCHAR(100) DEFAULT '',
    email VARCHAR(100) DEFAULT '',
    phone_number VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    note VARCHAR(100) DEFAULT '',
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20),
    total_money FLOAT CHECK(total_money >= 0)
);

ALTER TABLE orders ADD COLUMN `shipping_method` VARCHAR(100);
ALTER TABLE orders ADD COLUMN `shipping_address` VARCHAR(200);
ALTER TABLE orders ADD COLUMN `shipping_date` date;
ALTER TABLE orders ADD COLUMN `tracking_number` VARCHAR(100);
ALTER TABLE orders ADD COLUMN `payment_method` VARCHAR(100);
-- xóa 1 đơn hàng => xóa mềm => thêm trường active
ALTER TABLE orders ADD COLUMN active TINYINT(1);
-- trạng thái đơn hàng chỉ được phép nhận "1 số giá trị cụ thể"
ALTER TABLE orders
MODIFY COLUMN status ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled')
COMMENT 'Trạng thái đơn hàng';

CREATE TABLE order_details(
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    product_id INT,
    FOREIGN KEY (product_id) REFERENCES products(id),
    price FLOAT CHECK(price >= 0),
    number_of_products INT CHECK(number_of_products > 0),
    total_money FLOAT CHECK(total_money >= 0),
    color VARCHAR(20) DEFAULT ''
);

UPDATE products
SET thumbnail = (
    SELECT image_url
    FROM product_images
    WHERE products.id = product_images.product_id
    LIMIT 1
)

-- Bảng user devices (lưu FCM tokens)
CREATE TABLE user_devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    device_token VARCHAR(500) NOT NULL UNIQUE,
    device_type ENUM('MOBILE', 'WEB') NOT NULL,
    device_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_device_token (device_token)
);

-- Bảng notifications
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    type ENUM('ORDER', 'PROMOTION', 'SYSTEM', 'PAYMENT') NOT NULL,
    data JSON, -- Lưu thêm data như order_id, product_id...
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created_at (created_at)
);

-- Bảng notification templates (optional)
CREATE TABLE notification_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL UNIQUE,
    title_template VARCHAR(255) NOT NULL,
    body_template TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert templates
INSERT INTO notification_templates VALUES
(1, 'ORDER_CREATED', 'Đơn hàng mới #{orderId}', 'Bạn có đơn hàng mới với giá trị {totalAmount}đ', NOW()),
(2, 'ORDER_SHIPPED', 'Đơn hàng #{orderId} đã giao', 'Đơn hàng của bạn đang trên đường giao đến', NOW()),
(3, 'PAYMENT_SUCCESS', 'Thanh toán thành công', 'Bạn đã thanh toán thành công {amount}đ', NOW());
