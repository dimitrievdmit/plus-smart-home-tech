package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.DeliveryState;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.AddressMapper;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.AddressRepository;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;
    private final DeliveryCostCalculator costCalculator;

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        log.info("Планирование доставки для заказа {}", deliveryDto.getOrderId());

        // Сохраняем адреса
        Address fromAddress = AddressMapper.toEntity(deliveryDto.getFromAddress());
        Address toAddress = AddressMapper.toEntity(deliveryDto.getToAddress());
        fromAddress = addressRepository.save(fromAddress);
        toAddress = addressRepository.save(toAddress);

        // Создаём доставку через маппер с заполнением всех полей
        Delivery delivery = DeliveryMapper.toEntity(deliveryDto, fromAddress, toAddress);
        delivery = deliveryRepository.save(delivery);

        return DeliveryMapper.toDto(delivery);
    }

    public Double deliveryCost(OrderDto order) {
        log.info("Расчёт стоимости доставки для заказа {}", order.getOrderId());
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        return costCalculator.calculate(order, warehouseAddress);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        log.info("Товары переданы в доставку для заказа {}", orderId);
        Delivery delivery = getDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        log.info("Доставка успешно завершена для заказа {}", orderId);
        Delivery delivery = getDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        orderClient.internalDeliverySuccess(orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        log.info("Ошибка доставки для заказа {}", orderId);
        Delivery delivery = getDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        orderClient.deliveryFailed(orderId);
    }

    private Delivery getDeliveryByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка для заказа " + orderId + " не найдена"));
    }
}