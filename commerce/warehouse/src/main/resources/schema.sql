CREATE TABLE IF NOT EXISTS warehouse_product (
    product_id UUID PRIMARY KEY,
    quantity BIGINT NOT NULL DEFAULT 0,
    fragile BOOLEAN NOT NULL DEFAULT FALSE,
    width DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    depth DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS order_bookings (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL,
    delivery_id UUID
);
CREATE INDEX IF NOT EXISTS idx_order_bookings_order_id ON order_bookings(order_id);