-- Таблица адресов
CREATE TABLE IF NOT EXISTS addresses (
    id UUID PRIMARY KEY,
    country VARCHAR(255),
    city VARCHAR(255),
    street VARCHAR(255),
    house VARCHAR(255),
    flat VARCHAR(255)
);

-- Таблица доставок
CREATE TABLE IF NOT EXISTS deliveries (
    delivery_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    from_address_id UUID REFERENCES addresses(id),
    to_address_id UUID REFERENCES addresses(id),
    delivery_state VARCHAR(20) NOT NULL
);