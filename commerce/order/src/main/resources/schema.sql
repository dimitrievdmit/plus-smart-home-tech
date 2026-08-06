CREATE TABLE IF NOT EXISTS orders (
    order_id UUID PRIMARY KEY,
    shopping_cart_id UUID,
    state VARCHAR(20) NOT NULL,
    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile BOOLEAN,
    total_price DOUBLE PRECISION,
    delivery_price DOUBLE PRECISION,
    product_price DOUBLE PRECISION,
    payment_id UUID,
    delivery_id UUID,
    username VARCHAR(255),
    delivery_address_country VARCHAR(255),
    delivery_address_city VARCHAR(255),
    delivery_address_street VARCHAR(255),
    delivery_address_house VARCHAR(255),
    delivery_address_flat VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_orders_username ON orders(username);

CREATE TABLE IF NOT EXISTS order_products (
    order_id UUID REFERENCES orders(order_id),
    product_id UUID,
    quantity BIGINT,
    PRIMARY KEY (order_id, product_id)
);