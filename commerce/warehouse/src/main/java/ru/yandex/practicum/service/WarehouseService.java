package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {
    private final WarehouseProductRepository productRepository;
    private final ShoppingStoreClient storeClient;

    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest request) {
        log.info("Регистрация нового товара на складе: productId={}", request.getProductId());
        if (productRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Товар с таким ID уже зарегистрирован на складе");
        }
        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(request.getProductId());
        product.setFragile(request.isFragile());
        DimensionDto dim = request.getDimension();
        product.setWidth(dim.getWidth());
        product.setHeight(dim.getHeight());
        product.setDepth(dim.getDepth());
        product.setWeight(request.getWeight());
        product.setQuantity(0);
        productRepository.save(product);
        updateQuantityState(product.getProductId(), 0);
        log.info("Товар {} зарегистрирован", request.getProductId());
    }

    @Transactional
    public BookedProductsDto checkCart(ShoppingCartDto cartDto) {
        log.info("Проверка и резервирование корзины {}", cartDto.getShoppingCartId());
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        Map<UUID, Long> products = cartDto.getProducts();
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            long requiredQty = entry.getValue();

            WarehouseProduct product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductInShoppingCartLowQuantityInWarehouse("Товар " + productId + " отсутствует на складе"));
            if (product.getQuantity() < requiredQty) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточно товара " + productId + " на складе");
            }
        }

        // Все проверки пройдены – списываем товары
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            long requiredQty = entry.getValue();
            WarehouseProduct product = productRepository.findById(productId).orElseThrow();
            product.setQuantity(product.getQuantity() - requiredQty);
            productRepository.save(product);

            // Обновляем статус в shopping-store
            updateQuantityState(productId, product.getQuantity());

            totalWeight += product.getWeight() * requiredQty;
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requiredQty;
            if (product.isFragile()) hasFragile = true;
        }

        log.info("Корзина {} зарезервирована. Вес: {}, объём: {}, хрупкое: {}", cartDto.getShoppingCartId(), totalWeight, totalVolume, hasFragile);
        return new BookedProductsDto(totalWeight, totalVolume, hasFragile);
    }

    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        log.info("Добавление {} единиц товара {} на склад", request.getQuantity(), request.getProductId());
        WarehouseProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар не зарегистрирован на складе"));
        long newQuantity = product.getQuantity() + request.getQuantity();
        product.setQuantity(newQuantity);
        productRepository.save(product);
        updateQuantityState(product.getProductId(), newQuantity);
    }

    private void updateQuantityState(UUID productId, long quantity) {
        QuantityState state;
        if (quantity == 0) {
            state = QuantityState.ENDED;
        } else if (quantity < 10) {
            state = QuantityState.FEW;
        } else if (quantity <= 100) {
            state = QuantityState.ENOUGH;
        } else {
            state = QuantityState.MANY;
        }
        try {
            SetProductQuantityStateRequest request = new SetProductQuantityStateRequest(productId, state);
            storeClient.setProductQuantityState(request);
            log.info("Обновлён статус количества товара {}: {}", productId, state);
        } catch (Exception e) {
            log.info("Ошибка при обновлении статуса количества товара {}: {}", productId, state);
        }
    }
}