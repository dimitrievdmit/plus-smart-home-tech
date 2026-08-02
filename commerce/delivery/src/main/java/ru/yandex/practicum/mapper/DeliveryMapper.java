package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;

import java.util.UUID;

public final class DeliveryMapper {

    public static Delivery toEntity(DeliveryDto dto, Address fromAddress, Address toAddress) {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(dto.getDeliveryId() != null ? dto.getDeliveryId() : UUID.randomUUID());
        delivery.setOrderId(dto.getOrderId());
        delivery.setFromAddress(fromAddress);
        delivery.setToAddress(toAddress);
        delivery.setDeliveryState(dto.getDeliveryState());
        return delivery;
    }

    public static DeliveryDto toDto(Delivery delivery) {
        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setOrderId(delivery.getOrderId());
        dto.setFromAddress(AddressMapper.toDto(delivery.getFromAddress()));
        dto.setToAddress(AddressMapper.toDto(delivery.getToAddress()));
        dto.setDeliveryState(delivery.getDeliveryState());
        return dto;
    }
}