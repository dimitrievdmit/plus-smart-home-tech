package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.PaymentStatus;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient storeClient;
    private final OrderClient orderClient;

    @Value("${payment.tax-rate:0.1}")
    private double taxRate;

    public Double productCost(OrderDto order) {
        log.info("Расчёт стоимости товаров для заказа {}", order.getOrderId());
        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе нет товаров");
        }
        double total = 0.0;
        for (Map.Entry<UUID, Long> entry : order.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            long quantity = entry.getValue();
            Double price = storeClient.getProduct(productId).getPrice();
            total += price * quantity;
        }
        return total;
    }

    public Double getTotalCost(OrderDto order) {
        log.info("Расчёт полной стоимости заказа {}", order.getOrderId());
        if (order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не указана стоимость доставки");
        }
        Double productPrice = productCost(order);
        Double tax = productPrice * taxRate;
        return productPrice + tax + order.getDeliveryPrice();
    }

    @Transactional
    public PaymentDto payment(OrderDto order) {
        log.info("Формирование оплаты для заказа {}", order.getOrderId());
        Double productPrice = productCost(order);
        Double deliveryPrice = order.getDeliveryPrice();
        if (deliveryPrice == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не указана стоимость доставки");
        }
        Double tax = productPrice * taxRate;
        Double total = productPrice + tax + deliveryPrice;

        Payment payment = PaymentMapper.toEntity(order, productPrice, deliveryPrice, tax, total);
        payment = paymentRepository.save(payment);
        return PaymentMapper.toDto(payment);
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        log.info("Эмуляция успешной оплаты платежа {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Платёж не найден: " + paymentId));
        payment.setStatus(PaymentStatus.SUCCESS);
        orderClient.internalPaymentSuccess(payment.getOrderId());
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        log.info("Эмуляция отказа оплаты платежа {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Платёж не найден: " + paymentId));
        payment.setStatus(PaymentStatus.FAILED);
        orderClient.paymentFailed(payment.getOrderId());
    }
}