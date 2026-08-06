package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.OrderApi;
import ru.yandex.practicum.feign.fallback.OrderClientFallback;

@FeignClient(name = "order", configuration = FeignConfig.class, fallback = OrderClientFallback.class)
public interface OrderClient extends OrderApi {
}