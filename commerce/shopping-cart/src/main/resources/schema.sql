CREATE TABLE IF NOT EXISTS shopping_cart (
    shopping_cart_id UUID PRIMARY KEY,
    username VARCHAR NOT NULL,
    state VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_item (
    id UUID PRIMARY KEY,
    product_id UUID,
    quantity BIGINT,
    cart_id UUID REFERENCES shopping_cart(shopping_cart_id)
);