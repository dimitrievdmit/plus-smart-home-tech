package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.CartState;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final WarehouseClient warehouseClient;

    public ShoppingCartDto getCart(String username) {
        log.info("Получение корзины пользователя: {}", username);
        ShoppingCart cart = getOrCreateActiveCart(username);
        return mapToDto(cart);
    }

    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        log.info("Добавление товаров в корзину пользователя {}: {}", username, products.keySet());
        ShoppingCart cart = getOrCreateActiveCart(username);
        ShoppingCartDto cartDto = mapToDto(cart);
        // Слияние товаров
        Map<UUID, Long> merged = new HashMap<>(cartDto.getProducts());
        products.forEach((id, qty) -> merged.merge(id, qty, Long::sum));
        cartDto.setProducts(merged);
        // Проверка на складе
        warehouseClient.checkProductQuantityEnoughForShoppingCart(cartDto);
        // Если исключения от Circuit Breaker нет, обновляем элементы корзины
        updateCartItems(cart, merged);
        return mapToDto(cart);
    }

    public void deactivateCart(String username) {
        log.info("Деактивация корзины пользователя: {}", username);
        ShoppingCart cart = cartRepository.findByUsernameAndState(username, CartState.ACTIVE)
                .orElseThrow(() -> new NoProductsInShoppingCartException("Нет активной корзины"));
        cart.setState(CartState.DEACTIVATED);
        cartRepository.save(cart);
    }

    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {
        log.info("Удаление продуктов из корзины пользователя: {}", username);
        ShoppingCart cart = getOrCreateActiveCart(username);
        cart.getItems().removeIf(item -> productIds.contains(item.getProductId()));
        cartRepository.save(cart);
        return mapToDto(cart);
    }

    public ShoppingCartDto changeQuantity(String username, UUID productId, long newQuantity) {
        log.info("Изменить количество товаров в корзине пользователя: {}", username);
        ShoppingCart cart = getOrCreateActiveCart(username);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NoProductsInShoppingCartException("Товар отсутствует в корзине"));
        if (newQuantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(newQuantity);
        }
        cartRepository.save(cart);
        return mapToDto(cart);
    }

    private ShoppingCart createCart(String username) {
        log.info("Создание корзины пользователя: {}", username);
        ShoppingCart cart = new ShoppingCart();
        cart.setUsername(username);
        return cartRepository.save(cart);
    }

    private ShoppingCart getOrCreateActiveCart(String username) {
        return cartRepository.findByUsernameAndState(username, CartState.ACTIVE)
                .orElseGet(() -> createCart(username));
    }

    private void updateCartItems(ShoppingCart cart, Map<UUID, Long> products) {
        cart.getItems().clear();
        products.forEach((productId, qty) -> {
            CartItem item = new CartItem();
            item.setProductId(productId);
            item.setQuantity(qty);
            item.setCart(cart);
            cart.getItems().add(item);
        });
    }

    private ShoppingCartDto mapToDto(ShoppingCart cart) {
        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setShoppingCartId(cart.getShoppingCartId());
        Map<UUID, Long> productsMap = new HashMap<>();
        if (cart.getItems() != null) {
            cart.getItems().forEach(item -> productsMap.put(item.getProductId(), item.getQuantity()));
        }
        dto.setProducts(productsMap);
        return dto;
    }
}