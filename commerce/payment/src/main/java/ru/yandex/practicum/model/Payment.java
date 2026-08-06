package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.dto.PaymentStatus;

import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "product_cost")
    private Double productCost;

    @Column(name = "delivery_cost")
    private Double deliveryCost;

    @Column(name = "fee")
    private Double fee;

    @Column(name = "total_cost")
    private Double totalCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;
}