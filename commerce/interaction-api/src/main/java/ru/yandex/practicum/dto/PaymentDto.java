package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private UUID paymentId;
    private Double totalPayment;    // общая стоимость (с налогом и доставкой)
    private Double deliveryTotal;    // стоимость доставки
    private Double feeTotal;         // сумма налога
}