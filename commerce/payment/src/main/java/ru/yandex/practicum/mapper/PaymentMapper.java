package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.PaymentStatus;
import ru.yandex.practicum.model.Payment;

import java.util.UUID;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static Payment toEntity(OrderDto order, double productCost, double deliveryCost, double tax, double total) {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setOrderId(order.getOrderId());
        payment.setProductCost(productCost);
        payment.setDeliveryCost(deliveryCost);
        payment.setFee(tax);
        payment.setTotalCost(total);
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    public static PaymentDto toDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(payment.getPaymentId());
        dto.setTotalPayment(payment.getTotalCost());
        dto.setDeliveryTotal(payment.getDeliveryCost());
        dto.setFeeTotal(payment.getFee());
        return dto;
    }
}