package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartApi {

    @GetMapping("/api/v1/shopping-cart")
    ShoppingCartDto getShoppingCart(@RequestParam("username") String username);

    @PutMapping("/api/v1/shopping-cart")
    ShoppingCartDto addProductToShoppingCart(@RequestParam("username") String username,
                                             @RequestBody Map<UUID, Long> products);

    @DeleteMapping("/api/v1/shopping-cart")
    void deactivateCurrentShoppingCart(@RequestParam("username") String username);

    @PostMapping("/api/v1/shopping-cart/remove")
    ShoppingCartDto removeFromShoppingCart(@RequestParam("username") String username,
                                           @RequestBody List<UUID> productIds);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam("username") String username,
                                          @RequestBody ChangeProductQuantityRequest request);

    // В спецификации такого метода нет, но
    // ТЗ требует возможность просматривать уже добавленные позиции в деактивированных корзинах
    @GetMapping("/api/v1/shopping-cart/history")
    List<ShoppingCartDto> getDeactivatedCarts(@RequestParam("username") String username);
}