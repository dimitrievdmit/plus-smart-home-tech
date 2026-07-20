package ru.yandex.practicum.feign.fallback;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.ServiceUnavailableException;
import ru.yandex.practicum.feign.ShoppingCartClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ShoppingCartClientFallback implements ShoppingCartClient {
    private static final String SERVICE_NAME = "shopping-cart";

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public List<ShoppingCartDto> getDeactivatedCarts(String username) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }
}