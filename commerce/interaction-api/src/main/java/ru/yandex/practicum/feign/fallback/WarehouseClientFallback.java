package ru.yandex.practicum.feign.fallback;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.ServiceUnavailableException;
import ru.yandex.practicum.feign.WarehouseClient;

import java.util.Map;
import java.util.UUID;

@Component
public class WarehouseClientFallback implements WarehouseClient {
    private static final String SERVICE_NAME = "warehouse";

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cartDto) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }
}