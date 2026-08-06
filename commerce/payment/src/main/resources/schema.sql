CREATE TABLE IF NOT EXISTS payments (
    payment_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_cost DOUBLE PRECISION,
    delivery_cost DOUBLE PRECISION,
    fee DOUBLE PRECISION,
    total_cost DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL
);