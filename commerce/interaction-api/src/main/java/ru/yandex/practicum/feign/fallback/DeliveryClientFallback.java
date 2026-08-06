package ru.yandex.practicum.feign.fallback;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.exception.ServiceUnavailableException;
import ru.yandex.practicum.feign.DeliveryClient;

import java.util.UUID;

@Component
public class DeliveryClientFallback implements DeliveryClient {
    private static final String SERVICE_NAME = "delivery";

    @Override
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public Double deliveryCost(OrderDto order) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void deliveryPicked(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void deliverySuccessful(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void deliveryFailed(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }
}