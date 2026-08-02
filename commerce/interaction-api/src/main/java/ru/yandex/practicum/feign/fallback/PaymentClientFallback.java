package ru.yandex.practicum.feign.fallback;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.exception.ServiceUnavailableException;
import ru.yandex.practicum.feign.PaymentClient;

import java.util.UUID;

@Component
public class PaymentClientFallback implements PaymentClient {
    private static final String SERVICE_NAME = "payment";

    @Override
    public Double productCost(OrderDto order) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Double getTotalCost(OrderDto order) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public PaymentDto payment(OrderDto order) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }
}