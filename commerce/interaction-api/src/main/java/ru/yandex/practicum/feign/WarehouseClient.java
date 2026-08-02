package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.WarehouseApi;
import ru.yandex.practicum.feign.fallback.WarehouseClientFallback;

@FeignClient(name = "warehouse", fallback = WarehouseClientFallback.class,
        configuration = FeignConfig.class)
public interface WarehouseClient extends WarehouseApi {
}