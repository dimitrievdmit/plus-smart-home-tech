package ru.yandex.practicum.mapper;

import ru.yandex.practicum.model.OrderBooking;

import java.util.UUID;

public final class OrderBookingMapper {

    public static OrderBooking toEntity(UUID orderId, UUID productId, long quantity) {
        OrderBooking booking = new OrderBooking();
        booking.setId(UUID.randomUUID());
        booking.setOrderId(orderId);
        booking.setProductId(productId);
        booking.setQuantity(quantity);
        booking.setDeliveryId(null);  // до передачи в доставку
        return booking;
    }
}