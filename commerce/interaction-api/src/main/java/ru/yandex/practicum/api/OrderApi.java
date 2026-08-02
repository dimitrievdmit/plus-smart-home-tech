package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

public interface OrderApi {


    @GetMapping
    public List<OrderDto> getClientOrders(@RequestParam String username);

    @PutMapping
    public OrderDto createNewOrder(@RequestBody CreateNewOrderRequest request,
                                   @RequestParam String username);

    @PostMapping("/api/v1/order/return")
    public OrderDto productReturn(@RequestBody ProductReturnRequest request);

    @PostMapping("/api/v1/order/payment")
    public OrderDto payment(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/payment/failed")
    public OrderDto paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery")
    public OrderDto delivery(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    public OrderDto deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/completed")
    public OrderDto complete(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/total")
    public OrderDto calculateTotalCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    public OrderDto calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly")
    public OrderDto assembly(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly/failed")
    public OrderDto assemblyFailed(@RequestBody UUID orderId);

    // Внутренние эндпоинты для callback'ов (не входят в OpenAPI)
    @PostMapping("/api/v1/order/internal/payment/success")
    public OrderDto internalPaymentSuccess(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/internal/delivery/success")
    public OrderDto internalDeliverySuccess(@RequestBody UUID orderId);
}