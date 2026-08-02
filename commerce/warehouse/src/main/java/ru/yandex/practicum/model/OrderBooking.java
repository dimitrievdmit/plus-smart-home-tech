package ru.yandex.practicum.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "order_bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBooking {
    @Id
    private UUID id;                // уникальный идентификатор записи бронирования

    private UUID orderId;           // идентификатор заказа
    private UUID productId;         // идентификатор товара
    private long quantity;          // зарезервированное количество
    private UUID deliveryId;        // идентификатор доставки (может быть null до передачи в доставку)
}