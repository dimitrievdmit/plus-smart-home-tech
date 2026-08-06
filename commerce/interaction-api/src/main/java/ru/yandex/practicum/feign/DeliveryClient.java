package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.DeliveryApi;
import ru.yandex.practicum.feign.fallback.DeliveryClientFallback;

@FeignClient(name = "delivery", configuration = FeignConfig.class, fallback = DeliveryClientFallback.class)
public interface DeliveryClient extends DeliveryApi {
}