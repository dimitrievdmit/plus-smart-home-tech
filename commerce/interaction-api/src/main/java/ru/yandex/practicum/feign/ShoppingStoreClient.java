package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.ShoppingStoreApi;
import ru.yandex.practicum.feign.fallback.ShoppingStoreClientFallback;

@FeignClient(name = "shopping-store", fallback = ShoppingStoreClientFallback.class,
        configuration = FeignConfig.class)
public interface ShoppingStoreClient extends ShoppingStoreApi {
}