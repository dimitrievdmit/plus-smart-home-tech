package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.CartState;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final WarehouseClient warehouseClient;

    public ShoppingCartDto getCart(String username) {
        log.info("Получение корзины пользователя {}", username);
        ShoppingCart cart = cartRepository.findByUsernameAndState(username, CartState.ACTIVE)
                .orElseGet(() -> {
                    log.info("Создание новой корзины для {}", username);
                    return createCart(username);
                });
        return mapToDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        log.info("Добавление товаров в корзину {}: {}", username, products.keySet());
        ShoppingCart cart = getActiveCart(username).orElseGet(() -> createCart(username));

        // Формируем предполагаемое новое содержимое корзины
        Map<UUID, Long> currentProducts = getProductsMap(cart);
        products.forEach((id, qty) -> currentProducts.merge(id, qty, Long::sum));

        // Проверка на складе (теперь склад ещё и резервирует товар)
        ShoppingCartDto tempDto = new ShoppingCartDto(cart.getShoppingCartId(), new HashMap<>(currentProducts));
        warehouseClient.checkProductQuantityEnoughForShoppingCart(tempDto);

        // Обновляем элементы корзины в БД
        updateCartItems(cart, currentProducts);
        cartRepository.save(cart);
        return mapToDto(cart);
    }

    @Transactional
    public void deactivateCart(String username) {
        log.info("Деактивация корзины пользователя {}", username);
        ShoppingCart cart = cartRepository.findByUsernameAndState(username, CartState.ACTIVE)
                .orElseThrow(() -> new NoProductsInShoppingCartException("Нет активной корзины для деактивации"));
        cart.setState(CartState.DEACTIVATED);
    }

    @Transactional
    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {
        log.info("Удаление товаров из корзины {}: {}", username, productIds);
        ShoppingCart cart = getActiveCart(username).orElseGet(() -> createCart(username));
        cart.getItems().removeIf(item -> productIds.contains(item.getProductId()));
        return mapToDto(cart);
    }

    @Transactional
    public ShoppingCartDto changeQuantity(String username, UUID productId, long newQuantity) {
        log.info("Изменение количества товара {} в корзине {}: новое количество {}", productId, username, newQuantity);
        ShoppingCart cart = getActiveCart(username).orElseGet(() -> createCart(username));
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NoProductsInShoppingCartException("Товар отсутствует в корзине"));
        if (newQuantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(newQuantity);
        }
        return mapToDto(cart);
    }

    // В спецификации такого метода нет, но
    // ТЗ требует возможность просматривать уже добавленные позиции в деактивированных корзинах
    public List<ShoppingCartDto> getDeactivatedCarts(String username) {
        log.info("Запрос деактивированных корзин пользователя {}", username);
        List<ShoppingCart> deactivated = cartRepository.findAllByUsernameAndState(username, CartState.DEACTIVATED);
        return deactivated.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ShoppingCart createCart(String username) {
        log.info("Создание корзины пользователя: {}", username);
        ShoppingCart cart = new ShoppingCart();
        cart.setUsername(username);
        return cartRepository.save(cart);
    }

    private Optional<ShoppingCart> getActiveCart(String username) {
        return cartRepository.findByUsernameAndState(username, CartState.ACTIVE);
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

    private Map<UUID, Long> getProductsMap(ShoppingCart cart) {
        Map<UUID, Long> map = new HashMap<>();
        if (cart.getItems() != null) {
            cart.getItems().forEach(item -> map.put(item.getProductId(), item.getQuantity()));
        }
        return map;
    }

    private ShoppingCartDto mapToDto(ShoppingCart cart) {
        return new ShoppingCartDto(cart.getShoppingCartId(), getProductsMap(cart));
    }
}