-- ===================================================
-- Radiant Skin - Complete Database Setup Script
-- BCA Sem 5 Academic Project
-- ===================================================

-- 1. Create Database
CREATE DATABASE IF NOT EXISTS radiant_skin;
USE radiant_skin;

-- 2. Drop existing tables if needed (Clean Setup)
-- DROP TABLE IF EXISTS order_items, reviews, contact_messages, orders, products, categories, users;

-- ===================================================
-- Table 1: USERS (ગ્રાહકો અને એડમિનનો ડેટા)
-- ===================================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    city VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    role ENUM('customer', 'admin') DEFAULT 'customer',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- Table 2: CATEGORIES (પ્રોડક્ટ કેટેગરીઝ)
-- ===================================================
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_key VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL,
    color VARCHAR(20) DEFAULT '#F4DCC4',
    accent VARCHAR(20) DEFAULT '#C97B4A',
    shape VARCHAR(30) DEFAULT 'jar',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- Table 3: PRODUCTS (પ્રોડક્ટ્સની વિગતો અને કિંમત)
-- ===================================================
CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    category_key VARCHAR(50),
    price DECIMAL(10, 2) NOT NULL,
    old_price DECIMAL(10, 2),
    discount INT DEFAULT 0,
    rating DECIMAL(2, 1) DEFAULT 1.0,
    reviews_count INT DEFAULT 0,
    image_url TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_key) REFERENCES categories(category_key) ON DELETE SET NULL
);

-- ===================================================
-- Table 4: ORDERS (ગ્રાહકોના ઓર્ડર્સ)
-- ===================================================
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id INT,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(100),
    customer_phone VARCHAR(20),
    shipping_address TEXT,
    city VARCHAR(100),
    state VARCHAR(50) DEFAULT 'Gujarat',
    pincode VARCHAR(20),
    payment_method VARCHAR(50) DEFAULT 'Cash on Delivery',
    total_amount DECIMAL(10, 2) NOT NULL,
    order_status ENUM('Processing', 'Shipped', 'Delivered', 'Cancelled') DEFAULT 'Processing',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ===================================================
-- Table 5: ORDER_ITEMS (ઓર્ડરમાં રહેલી પ્રોડક્ટ્સ)
-- ===================================================
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id VARCHAR(20),
    product_name VARCHAR(150),
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- ===================================================
-- Table 6: REVIEWS (પ્રોડક્ટ રેટિંગ અને રિવ્યૂ)
-- ===================================================
CREATE TABLE IF NOT EXISTS reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    product_name VARCHAR(150),
    user_name VARCHAR(100) NOT NULL,
    user_email VARCHAR(100),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(150),
    comment TEXT,
    review_text TEXT,
    status VARCHAR(20) DEFAULT 'Approved',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ===================================================
-- Table 7: CONTACT_MESSAGES (સંપર્ક ફોર્મના મેસેજ)
-- ===================================================
CREATE TABLE IF NOT EXISTS contact_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    subject VARCHAR(150),
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- SAMPLE DATA INSERTS (ડેમો ડેટા ઉમેરો)
-- ===================================================

-- 1. Insert Default Categories
INSERT INTO categories (category_key, label, color, accent, shape) VALUES
('serum', 'Serum', '#F4DCC4', '#C97B4A', 'dropper'),
('cleanser', 'Face Wash / Cleanser', '#F3D9DC', '#B96B78', 'pump'),
('moisturizer', 'Moisturizer', '#E4DCEF', '#7C5B99', 'jar'),
('sunscreen', 'Sunscreen', '#F7ECC8', '#C99A2E', 'tube'),
('nightcream', 'Night Cream', '#E7D4DA', '#6B4550', 'jar'),
('toner', 'Toner', '#DCEAE6', '#4E8A7A', 'spray')
ON DUPLICATE KEY UPDATE label=VALUES(label);

-- 2. Insert Admin & Demo Users
INSERT INTO users (name, email, phone, city, password, role) VALUES
('Admin User', 'admin@radiantskin.com', '9876543210', 'Rajkot', 'admin123', 'admin'),
('Customer Demo', 'user@gmail.com', '9825012345', 'Upleta', 'user123', 'customer')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Insert Sample Products
INSERT INTO products (id, name, brand, category_key, price, old_price, discount, rating, reviews_count, image_url, description) VALUES
('P001', 'Velvet Cloud Serum', 'Petal & Bloom', 'serum', 284.00, NULL, 0, 1.0, 0, 'images/P001.jpg', 'A gentle, dermatologist-tested serum crafted with botanical extracts.'),
('P002', 'Golden Hour Serum', 'Verdant', 'serum', 346.00, NULL, 0, 1.0, 0, 'images/P002.jpg', 'Restores natural luminosity. Lightweight and fast-absorbing.'),
('P011', 'Velvet Cloud Cleanser', 'Petal & Bloom', 'cleanser', 249.00, NULL, 0, 1.0, 0, 'images/P011.jpg', 'Deep pore cleansing formula for clear and radiant skin.'),
('P021', 'Cloud Whip Moisturizer', 'Cloudberry', 'moisturizer', 380.00, 420.00, 10, 1.0, 0, 'images/P021.jpg', 'Hydrating daily moisturizer with vitamin nourishment.'),
('P031', 'Azure Mist Sunscreen SPF50', 'Verdant', 'sunscreen', 450.00, NULL, 0, 1.0, 0, 'images/P031.jpg', 'Broad spectrum UV protection without white cast.'),
('P041', 'Twilight Renewal Cream', 'Petal & Bloom', 'nightcream', 499.00, 599.00, 16, 1.0, 0, 'images/P041.jpg', 'Overnight skin repair and deep hydration cream.'),
('P051', 'Petal Infusion Toner', 'Petal & Bloom', 'toner', 299.00, NULL, 0, 1.0, 0, 'images/P051.jpg', 'Pore tightening and pH balance botanical facial toner.')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Verify all tables
SHOW TABLES;
