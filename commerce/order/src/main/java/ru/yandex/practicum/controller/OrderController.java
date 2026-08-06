package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.OrderApi;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController implements OrderApi {
    private final OrderService orderService;

    @Override
    public List<OrderDto> getClientOrders(@RequestParam String username) {
        return orderService.getClientOrders(username);
    }

    @Override
    public OrderDto createNewOrder(@RequestBody CreateNewOrderRequest request,
                                   @RequestParam String username) {
        return orderService.createNewOrder(request, username);
    }

    @Override
    public OrderDto productReturn(@RequestBody ProductReturnRequest request) {
        return orderService.productReturn(request);
    }

    @Override
    public OrderDto payment(@RequestBody UUID orderId) {
        return orderService.payment(orderId);
    }

    @Override
    public OrderDto paymentFailed(@RequestBody UUID orderId) {
        return orderService.paymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(@RequestBody UUID orderId) {
        return orderService.delivery(orderId);
    }

    @Override
    public OrderDto deliveryFailed(@RequestBody UUID orderId) {
        return orderService.deliveryFailed(orderId);
    }

    @Override
    public OrderDto complete(@RequestBody UUID orderId) {
        return orderService.complete(orderId);
    }

    @Override
    public OrderDto calculateTotalCost(@RequestBody UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }

    @Override
    public OrderDto calculateDeliveryCost(@RequestBody UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    @Override
    public OrderDto assembly(@RequestBody UUID orderId) {
        return orderService.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(@RequestBody UUID orderId) {
        return orderService.assemblyFailed(orderId);
    }

    // Внутренние эндпоинты для callback'ов (не входят в OpenAPI)
    @Override
    public OrderDto internalPaymentSuccess(@RequestBody UUID orderId) {
        return orderService.paymentSuccess(orderId);
    }

    @Override
    public OrderDto internalDeliverySuccess(@RequestBody UUID orderId) {
        return orderService.deliverySuccess(orderId);
    }
}