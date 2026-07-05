
-- Inventory Schema

CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE IF NOT EXISTS inventory.warehouses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS inventory.products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    unit_price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS inventory.stock (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES inventory.products(id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES inventory.warehouses(id),
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    low_stock_threshold INT NOT NULL DEFAULT 10,
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(product_id, warehouse_id)
);

-- Order Schema

CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE IF NOT EXISTS orders.orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(20) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order.order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders.orders(id) ON DELETE CASCADE
    product_id BIGINT NOT NULL,
    product_sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL
);

-- Seed Data

INSERT INTO inventory.warehouses (name, location)
VALUES ('Main Warehouse', 'Mexico City, CDMX')
ON CONFLICT DO NOTHING;

INSERT INTO inventory.products (sku, name, description, unit_price, category) VALUES
    ('LAPTOP-PRO-16', 'Laptop Pro 16"', 'High-performance laptop for developers', 25000.00, 'Electronics'),
    ('MOUSE-WIRELESS', 'Wireless Mouse', 'Ergonomic wireless mouse', 450.00, 'Peripherals'),
    ('KEYBOARD-MECH', 'Mechanical Keyboard', 'Tactile switches, TKL layout', 1200.00, 'Peripherals'),
    ('MONITOR-27-4K', '27" 4K Monitor', 'IPS panel, USB-C', 8500.00, 'Electronics'),
    ('HDMI-CABLE-2M', 'HDMI Cable 2m', 'HDMI 2.1 certified', 180.00, 'Accessories')
ON CONFLICT (sku) DO NOTHING;

INSERT INTO inventory.stock (product_id, warehouse_id, quantity, low_stock_threshold)
SELECT p.id, 1, 50, 10
FROM inventory.products p
ON CONFLICT (product_id, warehouse_id) DO NOTHING;
