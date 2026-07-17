package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feign.ShoppingCartClient;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartClient {
    private final ShoppingCartService service;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        checkUsername(username);
        return service.getCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        checkUsername(username);
        return service.addProducts(username, products);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        checkUsername(username);
        service.deactivateCart(username);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        checkUsername(username);
        return service.removeProducts(username, productIds);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        checkUsername(username);
        return service.changeQuantity(username, request.getProductId(), request.getNewQuantity());
    }

    // В спецификации такого метода нет, но
    // ТЗ требует возможность просматривать уже добавленные позиции в деактивированных корзинах
    @GetMapping("/history")
    public List<ShoppingCartDto> getDeactivatedCarts(@RequestParam("username") String username) {
        checkUsername(username);
        return service.getDeactivatedCarts(username);
    }

    private void checkUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя не должно быть пустым");
        }
    }
}