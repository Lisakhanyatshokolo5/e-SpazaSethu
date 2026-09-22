-- ============================================================
-- e-SpazaSethu — MySQL Database Schema
-- Version: 1.0
-- Description: Full schema for POS & Inventory Management System
-- Run this on a fresh espaza_db database
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS espaza_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE espaza_db;

-- ============================================================
-- TABLE ORDER MATTERS — FK dependencies created first
-- 1. category
-- 2. user
-- 3. product        (FK → category)
-- 4. sale           (FK → user)
-- 5. sale_item      (FK → sale, product)
-- 6. stock_movement (FK → product, user)
-- 7. stocktake      (FK → user)
-- 8. stocktake_item (FK → stocktake, product)
-- ============================================================


-- ------------------------------------------------------------
-- 1. CATEGORY
-- ------------------------------------------------------------
CREATE TABLE category (
  categoryId    CHAR(36)        NOT NULL,
  name          VARCHAR(100)    NOT NULL,
  description   TEXT,
  createdAt     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_category     PRIMARY KEY (categoryId),
  CONSTRAINT uq_category_name UNIQUE (name)
);


-- ------------------------------------------------------------
-- 2. USER
-- ------------------------------------------------------------
CREATE TABLE user (
  userId        CHAR(36)        NOT NULL,
  username      VARCHAR(50)     NOT NULL,
  passwordHash  VARCHAR(255)    NOT NULL,
  role          ENUM('ADMIN', 'CASHIER') NOT NULL DEFAULT 'CASHIER',
  isActive      TINYINT(1)      NOT NULL DEFAULT 1,
  lastLogin     DATETIME,
  createdAt     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_user       PRIMARY KEY (userId),
  CONSTRAINT uq_username   UNIQUE (username)
);


-- ------------------------------------------------------------
-- 3. PRODUCT
-- ------------------------------------------------------------
CREATE TABLE product (
  productId         CHAR(36)        NOT NULL,
  categoryId        CHAR(36),
  name              VARCHAR(200)    NOT NULL,
  barcode           VARCHAR(100),
  sellingPrice      DECIMAL(10, 2)  NOT NULL,
  costPrice         DECIMAL(10, 2),
  stockQuantity     INT             NOT NULL DEFAULT 0,
  lowStockThreshold INT,
  description       TEXT,
  isActive          TINYINT(1)      NOT NULL DEFAULT 1,
  createdAt         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_product           PRIMARY KEY (productId),
  CONSTRAINT uq_product_barcode   UNIQUE (barcode),
  CONSTRAINT fk_product_category  FOREIGN KEY (categoryId) REFERENCES category (categoryId) ON DELETE SET NULL,
  CONSTRAINT chk_selling_price    CHECK (sellingPrice >= 0),
  CONSTRAINT chk_cost_price       CHECK (costPrice IS NULL OR costPrice >= 0),
  CONSTRAINT chk_stock_qty        CHECK (stockQuantity >= 0),
  CONSTRAINT chk_low_stock        CHECK (lowStockThreshold IS NULL OR lowStockThreshold >= 0)
);

-- Index for product search by name
CREATE INDEX idx_product_name     ON product (name);
-- Index for barcode lookup (POS scanning)
CREATE INDEX idx_product_barcode  ON product (barcode);
-- Index for filtering active products
CREATE INDEX idx_product_active   ON product (isActive);
-- Index for low-stock queries
CREATE INDEX idx_product_low_stock ON product (stockQuantity, lowStockThreshold);


-- ------------------------------------------------------------
-- 4. SALE
-- ------------------------------------------------------------
CREATE TABLE sale (
  saleId          CHAR(36)        NOT NULL,
  userId          CHAR(36)        NOT NULL,
  saleDateTime    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  totalAmount     DECIMAL(10, 2)  NOT NULL,
  paymentMethod   ENUM('CASH', 'CARD', 'MOBILE_PAYMENT') NOT NULL,
  status          ENUM('PENDING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'COMPLETED',
  notes           TEXT,
  createdAt       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_sale        PRIMARY KEY (saleId),
  CONSTRAINT fk_sale_user   FOREIGN KEY (userId) REFERENCES user (userId),
  CONSTRAINT chk_total      CHECK (totalAmount >= 0)
);

-- Index for date-range queries (sales history, dashboard)
CREATE INDEX idx_sale_datetime  ON sale (saleDateTime);
-- Index for filtering by user
CREATE INDEX idx_sale_user      ON sale (userId);
-- Composite index for dashboard queries (status + date)
CREATE INDEX idx_sale_status_date ON sale (status, saleDateTime);


-- ------------------------------------------------------------
-- 5. SALE_ITEM
-- ------------------------------------------------------------
CREATE TABLE sale_item (
  saleItemId    CHAR(36)        NOT NULL,
  saleId        CHAR(36)        NOT NULL,
  productId     CHAR(36)        NOT NULL,
  quantity      INT             NOT NULL,
  unitPrice     DECIMAL(10, 2)  NOT NULL,
  subtotal      DECIMAL(10, 2)  NOT NULL,

  CONSTRAINT pk_sale_item         PRIMARY KEY (saleItemId),
  CONSTRAINT fk_sale_item_sale    FOREIGN KEY (saleId)    REFERENCES sale (saleId)    ON DELETE CASCADE,
  CONSTRAINT fk_sale_item_product FOREIGN KEY (productId) REFERENCES product (productId),
  CONSTRAINT chk_quantity         CHECK (quantity > 0),
  CONSTRAINT chk_unit_price       CHECK (unitPrice >= 0),
  CONSTRAINT chk_subtotal         CHECK (subtotal >= 0)
);

-- Index for loading all items in a sale
CREATE INDEX idx_sale_item_sale       ON sale_item (saleId);
-- Index for top-products report (group by product across sales)
CREATE INDEX idx_sale_item_product    ON sale_item (productId);


-- ------------------------------------------------------------
-- 6. STOCK_MOVEMENT
-- ------------------------------------------------------------
CREATE TABLE stock_movement (
  movementId      CHAR(36)        NOT NULL,
  productId       CHAR(36)        NOT NULL,
  createdBy       CHAR(36)        NOT NULL,
  quantityChange  INT             NOT NULL,
  movementType    ENUM('SALE', 'ADJUSTMENT', 'STOCKTAKE_CORRECTION') NOT NULL,
  referenceId     CHAR(36),
  notes           TEXT,
  createdAt       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_stock_movement          PRIMARY KEY (movementId),
  CONSTRAINT fk_movement_product        FOREIGN KEY (productId)  REFERENCES product (productId),
  CONSTRAINT fk_movement_user           FOREIGN KEY (createdBy)  REFERENCES user (userId)
  -- referenceId is intentionally not a strict FK — it can point to a sale or stocktake
  -- depending on movementType. Application layer resolves the reference.
);

-- Index for audit log queries (filter by product)
CREATE INDEX idx_movement_product   ON stock_movement (productId);
-- Index for date-range filtering on the audit log
CREATE INDEX idx_movement_date      ON stock_movement (createdAt);
-- Index for filtering by movement type
CREATE INDEX idx_movement_type      ON stock_movement (movementType);
-- Composite index for product history queries
CREATE INDEX idx_movement_product_date ON stock_movement (productId, createdAt);


-- ------------------------------------------------------------
-- 7. STOCKTAKE
-- ------------------------------------------------------------
CREATE TABLE stocktake (
  stocktakeId   CHAR(36)        NOT NULL,
  conductedBy   CHAR(36)        NOT NULL,
  startedAt     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completedAt   DATETIME,
  status        ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',
  notes         TEXT,

  CONSTRAINT pk_stocktake       PRIMARY KEY (stocktakeId),
  CONSTRAINT fk_stocktake_user  FOREIGN KEY (conductedBy) REFERENCES user (userId)
);

-- Index for finding in-progress stocktakes per user
CREATE INDEX idx_stocktake_user_status ON stocktake (conductedBy, status);
-- Index for listing stocktakes by date
CREATE INDEX idx_stocktake_started     ON stocktake (startedAt);


-- ------------------------------------------------------------
-- 8. STOCKTAKE_ITEM
-- ------------------------------------------------------------
CREATE TABLE stocktake_item (
  stocktakeItemId   CHAR(36)    NOT NULL,
  stocktakeId       CHAR(36)    NOT NULL,
  productId         CHAR(36)    NOT NULL,
  systemQuantity    INT         NOT NULL,
  countedQuantity   INT,
  discrepancy       INT,
  adjusted          TINYINT(1)  NOT NULL DEFAULT 0,

  CONSTRAINT pk_stocktake_item            PRIMARY KEY (stocktakeItemId),
  CONSTRAINT fk_stocktake_item_stocktake  FOREIGN KEY (stocktakeId) REFERENCES stocktake (stocktakeId) ON DELETE CASCADE,
  CONSTRAINT fk_stocktake_item_product    FOREIGN KEY (productId)   REFERENCES product (productId),
  -- Each product appears only once per stocktake
  CONSTRAINT uq_stocktake_product         UNIQUE (stocktakeId, productId)
);

-- Index for loading all items in a stocktake
CREATE INDEX idx_stocktake_item_stocktake ON stocktake_item (stocktakeId);
-- Index for discrepancy queries (filter out nulls and zeros)
CREATE INDEX idx_stocktake_item_discrep   ON stocktake_item (stocktakeId, discrepancy);


-- ============================================================
-- SEED DATA — Default admin user for local development
-- Password is BCrypt hash of "Admin@123"
-- CHANGE THIS BEFORE DEPLOYING TO ANY SHARED ENVIRONMENT
-- ============================================================

INSERT INTO user (userId, username, passwordHash, role, isActive, createdAt)
VALUES (
  'a0000000-0000-0000-0000-000000000001',
  'admin',
  '$2a$12$kVmhpJTzFYoLjbpbVQZFCOjh6FHmVHQv3JZMQpDRPrDnOWnCcg3.6',
  'ADMIN',
  1,
  NOW()
);

-- ============================================================
-- SEED DATA — Sample categories (optional, can be deleted)
-- ============================================================

INSERT INTO category (categoryId, name, description, createdAt) VALUES
  ('c0000000-0000-0000-0000-000000000001', 'Beverages',  'Cold drinks, juices, water',        NOW()),
  ('c0000000-0000-0000-0000-000000000002', 'Snacks',     'Chips, biscuits, sweets, chocolates', NOW()),
  ('c0000000-0000-0000-0000-000000000003', 'Airtime',    'Prepaid airtime and data vouchers',   NOW()),
  ('c0000000-0000-0000-0000-000000000004', 'Household',  'Cleaning products, toiletries',       NOW()),
  ('c0000000-0000-0000-0000-000000000005', 'Bread & Dairy', 'Bread, milk, eggs, butter',       NOW());

-- ============================================================
-- SEED DATA — Sample products (optional, useful for testing)
-- ============================================================

INSERT INTO product (productId, categoryId, name, barcode, sellingPrice, costPrice, stockQuantity, lowStockThreshold, isActive, createdAt, updatedAt) VALUES
  ('p0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'Coca-Cola 500ml',    '6001056000006', 15.00, 10.00, 48, 12, 1, NOW(), NOW()),
  ('p0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'Fanta Orange 500ml', '6001056000013', 15.00, 10.00, 36, 12, 1, NOW(), NOW()),
  ('p0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 'Water 500ml',        '6009705522009', 8.00,  5.00,  60, 20, 1, NOW(), NOW()),
  ('p0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000002', 'Lays Chips 120g',    '6001480005008', 22.00, 15.00, 24, 6,  1, NOW(), NOW()),
  ('p0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000002', 'Oreos 154g',         '7622210044860', 28.00, 19.00, 18, 6,  1, NOW(), NOW()),
  ('p0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000005', 'Albany Bread 700g',  '6001499000055', 19.99, 14.00, 10, 5,  1, NOW(), NOW()),
  ('p0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000005', 'Clover Milk 1L',     '6001209000020', 24.99, 18.00, 15, 5,  1, NOW(), NOW()),
  ('p0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000003', 'Vodacom R10 Airtime', NULL,           10.00, 10.00, 50, 10, 1, NOW(), NOW());

-- ============================================================
-- END OF SCHEMA
-- ============================================================
