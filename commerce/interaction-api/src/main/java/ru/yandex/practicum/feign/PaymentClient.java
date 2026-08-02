package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.PaymentApi;
import ru.yandex.practicum.feign.fallback.PaymentClientFallback;

@FeignClient(name = "payment", configuration = FeignConfig.class, fallback = PaymentClientFallback.class)
public interface PaymentClient extends PaymentApi {
}