package ru.yandex.practicum.feign.fallback;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;
import ru.yandex.practicum.exception.ServiceUnavailableException;
import ru.yandex.practicum.feign.OrderClient;

import java.util.List;
import java.util.UUID;

@Component
public class OrderClientFallback implements OrderClient {
    private static final String SERVICE_NAME = "order";

    @Override
    public List<OrderDto> getClientOrders(String username) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request, String username) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto complete(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto internalPaymentSuccess(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public OrderDto internalDeliverySuccess(UUID orderId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }
}