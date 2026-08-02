package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.OrderBooking;

import java.util.List;
import java.util.UUID;

public interface OrderBookingRepository extends JpaRepository<OrderBooking, UUID> {

    List<OrderBooking> findByOrderId(UUID orderId);

    @Modifying
    @Query("UPDATE OrderBooking ob SET ob.deliveryId = :deliveryId WHERE ob.orderId = :orderId")
    void updateDeliveryIdByOrderId(@Param("orderId") UUID orderId, @Param("deliveryId") UUID deliveryId);
}