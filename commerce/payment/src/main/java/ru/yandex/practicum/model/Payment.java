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
    private UUID paymentId;

    private UUID orderId;

    private Double productCost;
    private Double deliveryCost;
    private Double fee;
    private Double totalCost;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}