package ru.yandex.practicum.feign.fallback;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.ServiceUnavailableException;
import ru.yandex.practicum.feign.ShoppingStoreClient;

import java.util.UUID;

@Component
public class ShoppingStoreClientFallback implements ShoppingStoreClient {
    private static final String SERVICE_NAME = "shopping-store";

    @Override
    public PageProductDto getProducts(ProductCategory category, int page, int size, String[] sort) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public boolean removeProductFromStore(UUID productId) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }

    @Override
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        throw new ServiceUnavailableException(SERVICE_NAME);
    }
}