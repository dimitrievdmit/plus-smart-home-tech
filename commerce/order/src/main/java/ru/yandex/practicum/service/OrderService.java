package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feign.DeliveryClient;
import ru.yandex.practicum.feign.PaymentClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request, String username) {
        log.info("Создание заказа для пользователя {}", username);

        // 1. Проверка наличия товаров на складе (без списания)
        BookedProductsDto booked = warehouseClient.checkProductQuantityEnoughForShoppingCart(request.getShoppingCart());

        // 2. Создание заказа в статусе NEW
        Order order = OrderMapper.toEntity(request, username);
        OrderMapper.enrichWithBookedData(order, booked);
        order = orderRepository.save(order);

        // 3. Резервирование товаров на складе (сборка)
        AssemblyProductsForOrderRequest assemblyRequest = new AssemblyProductsForOrderRequest();
        assemblyRequest.setProducts(order.getProducts());
        assemblyRequest.setOrderId(order.getOrderId());
        warehouseClient.assemblyProductsForOrder(assemblyRequest);

        log.info("Заказ {} создан и собран", order.getOrderId());
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Расчёт стоимости доставки для заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        OrderDto orderDto = OrderMapper.toDto(order);
        Double cost = deliveryClient.deliveryCost(orderDto);
        order.setDeliveryPrice(cost);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Расчёт полной стоимости заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        OrderDto orderDto = OrderMapper.toDto(order);
        Double productCost = paymentClient.productCost(orderDto);
        Double total = paymentClient.getTotalCost(orderDto);
        OrderMapper.updateTotalCost(order, productCost, total);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        log.info("Оплата заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        order.setState(OrderState.ON_PAYMENT);
        OrderDto orderDto = OrderMapper.toDto(order);
        PaymentDto payment = paymentClient.payment(orderDto);
        order.setPaymentId(payment.getPaymentId());
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto paymentSuccess(UUID orderId) {
        log.info("Успешная оплата заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        order.setState(OrderState.PAID);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Ошибка оплаты заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        log.info("Подтверждение сборки заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        // Сборка уже выполнена при создании, просто меняем статус
        order.setState(OrderState.ASSEMBLED);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Ошибка сборки заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        log.info("Оформление доставки для заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);

        // Получаем адрес склада и адрес доставки
        AddressDto fromAddress = warehouseClient.getWarehouseAddress();
        AddressDto toAddress = order.getDeliveryAddress();
        if (toAddress == null) {
            throw new IllegalArgumentException("Адрес доставки не указан для заказа " + orderId);
        }

        // Создаём доставку
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setOrderId(orderId);
        deliveryDto.setFromAddress(fromAddress);
        deliveryDto.setToAddress(toAddress);
        DeliveryDto createdDelivery = deliveryClient.planDelivery(deliveryDto);

        order.setDeliveryId(createdDelivery.getDeliveryId());
        order.setState(OrderState.ON_DELIVERY);

        // Передаём товары в доставку на складе
        ShippedToDeliveryRequest shippedRequest = new ShippedToDeliveryRequest(orderId, createdDelivery.getDeliveryId());
        warehouseClient.shippedToDelivery(shippedRequest);

        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto deliverySuccess(UUID orderId) {
        log.info("Доставка успешно выполнена для заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        order.setState(OrderState.DELIVERED);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Ошибка доставки для заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        log.info("Завершение заказа {}", orderId);
        Order order = getOrderOrThrow(orderId);
        order.setState(OrderState.COMPLETED);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("Возврат товаров по заказу {}", request.getOrderId());
        Order order = getOrderOrThrow(request.getOrderId());
        order.setState(OrderState.PRODUCT_RETURNED);
        warehouseClient.acceptReturn(request.getProducts());
        return OrderMapper.toDto(order);
    }

    public List<OrderDto> getClientOrders(String username) {
        log.info("Получение заказов пользователя {}", username);
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя не должно быть пустым");
        }
        return orderRepository.findByUsername(username)
                .stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    private Order getOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ не найден: " + orderId));
    }
}