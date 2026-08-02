package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.OrderDto;

@Component
@Slf4j
public class DeliveryCostCalculator {

    @Value("${delivery.base-cost:5.0}")
    private double baseCost;

    @Value("${delivery.weight-coefficient:0.3}")
    private double weightCoefficient;

    @Value("${delivery.volume-coefficient:0.2}")
    private double volumeCoefficient;

    @Value("${delivery.fragile-coefficient:0.2}")
    private double fragileCoefficient;

    @Value("${delivery.address-mismatch-coefficient:0.2}")
    private double addressMismatchCoefficient;

    @Value("${delivery.address1-multiplier:1}")
    private int address1Multiplier;

    @Value("${delivery.address2-multiplier:2}")
    private int address2Multiplier;

    private static final String ADDRESS_1 = "ADDRESS_1";
    private static final String ADDRESS_2 = "ADDRESS_2";

    public double calculate(OrderDto order, AddressDto warehouseAddress) {
        log.debug("Расчёт стоимости доставки для заказа {}", order.getOrderId());

        AddressDto deliveryAddress = order.getDeliveryAddress();
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Адрес доставки не указан в заказе " + order.getOrderId());
        }

        double cost;

        String warehouseStreet = warehouseAddress.getStreet();
        if (warehouseStreet != null && warehouseStreet.contains(ADDRESS_1)) {
            cost = baseCost * address1Multiplier + baseCost;
        } else if (warehouseStreet != null && warehouseStreet.contains(ADDRESS_2)) {
            cost = baseCost * address2Multiplier + baseCost;
        } else {
            cost = baseCost * address1Multiplier + baseCost;
        }

        if (Boolean.TRUE.equals(order.getFragile())) {
            cost += cost * fragileCoefficient;
        }

        if (order.getDeliveryWeight() != null) {
            cost += order.getDeliveryWeight() * weightCoefficient;
        }

        if (order.getDeliveryVolume() != null) {
            cost += order.getDeliveryVolume() * volumeCoefficient;
        }

        String deliveryStreet = deliveryAddress.getStreet();
        if (warehouseStreet != null && !warehouseStreet.equals(deliveryStreet)) {
            cost += cost * addressMismatchCoefficient;
        }

        log.debug("Стоимость доставки рассчитана: {}", cost);
        return cost;
    }
}