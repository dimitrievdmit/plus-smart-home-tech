package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.OrderState;
import ru.yandex.practicum.model.Order;

import java.util.UUID;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(CreateNewOrderRequest request, String username) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setShoppingCartId(request.getShoppingCart().getShoppingCartId());
        order.setProducts(request.getShoppingCart().getProducts());
        order.setState(OrderState.NEW);
        order.setUsername(username);
        order.setDeliveryAddress(request.getDeliveryAddress());
        return order;
    }

    // Заполнение заказа данными о доставке и весе/объёме после проверки склада
    public static void enrichWithBookedData(Order order, BookedProductsDto booked) {
        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.isFragile());
        // Начальные значения стоимости (будут пересчитаны позже)
        order.setProductPrice(0.0);
        order.setDeliveryPrice(0.0);
        order.setTotalPrice(0.0);
    }

    public static OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setShoppingCartId(order.getShoppingCartId());
        dto.setProducts(order.getProducts());
        dto.setPaymentId(order.getPaymentId());
        dto.setDeliveryId(order.getDeliveryId());
        dto.setState(order.getState());
        dto.setDeliveryWeight(order.getDeliveryWeight());
        dto.setDeliveryVolume(order.getDeliveryVolume());
        dto.setFragile(order.getFragile());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDeliveryPrice(order.getDeliveryPrice());
        dto.setProductPrice(order.getProductPrice());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        return dto;
    }

    // Обновление заказа после расчёта полной стоимости
    public static void updateTotalCost(Order order, Double productCost, Double totalCost) {
        order.setProductPrice(productCost);
        order.setTotalPrice(totalCost);
    }
}