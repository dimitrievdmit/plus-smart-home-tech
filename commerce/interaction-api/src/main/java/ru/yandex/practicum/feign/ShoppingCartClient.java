package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.ShoppingCartApi;
import ru.yandex.practicum.feign.fallback.ShoppingCartClientFallback;

@FeignClient(name = "shopping-cart", fallback = ShoppingCartClientFallback.class,
        configuration = FeignConfig.class)
public interface ShoppingCartClient extends ShoppingCartApi {
}