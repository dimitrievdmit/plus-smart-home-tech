package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController {
    private final ShoppingCartService service;

    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam("username") String username) {
        checkUsername(username);
        return service.getCart(username);
    }

    @PutMapping
    public ShoppingCartDto addProductToShoppingCart(@RequestParam("username") String username,
                                                    @RequestBody Map<UUID, Long> products) {
        checkUsername(username);
        return service.addProducts(username, products);
    }

    @DeleteMapping
    public void deactivateCurrentShoppingCart(@RequestParam("username") String username) {
        checkUsername(username);
        service.deactivateCart(username);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeFromShoppingCart(@RequestParam("username") String username,
                                                  @RequestBody List<UUID> productIds) {
        checkUsername(username);
        return service.removeProducts(username, productIds);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam("username") String username,
                                                 @RequestBody ChangeProductQuantityRequest request) {
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