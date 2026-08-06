package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.PaymentApi;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Override
    public Double productCost(@RequestBody OrderDto order) {
        return paymentService.productCost(order);
    }

    @Override
    public Double getTotalCost(@RequestBody OrderDto order) {
        return paymentService.getTotalCost(order);
    }

    @Override
    public PaymentDto payment(@RequestBody OrderDto order) {
        return paymentService.payment(order);
    }

    @Override
    public void paymentSuccess(@RequestBody UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public void paymentFailed(@RequestBody UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}