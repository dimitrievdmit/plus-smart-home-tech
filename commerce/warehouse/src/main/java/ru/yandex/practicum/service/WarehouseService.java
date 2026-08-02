package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.BookingNotFoundException;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.OrderBookingMapper;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.OrderBookingRepository;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {
    private final WarehouseProductRepository productRepository;
    private final OrderBookingRepository bookingRepository;
    private final ShoppingStoreClient storeClient;

    // переменные для определения статуса количества
    @Value("${delivery.base-cost:10}")
    private long fewThreshold;
    @Value("${delivery.base-cost:100}")
    private long enoughThreshold;

    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest request) {
        log.info("Регистрация нового товара на складе: productId={}", request.getProductId());
        if (productRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Товар с таким ID уже зарегистрирован на складе");
        }
        WarehouseProduct product = WarehouseProductMapper.toEntity(request);
        productRepository.save(product);
        updateQuantityState(product.getProductId(), 0);
        log.info("Товар {} зарегистрирован", request.getProductId());
    }

    @Transactional
    public BookedProductsDto checkCart(ShoppingCartDto cartDto) {
        log.info("Проверка наличия товаров для корзины {}", cartDto.getShoppingCartId());
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        Map<UUID, Long> products = cartDto.getProducts();
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            long requiredQty = entry.getValue();

            // Проверяем наличие товара с достаточным количеством
            WarehouseProduct product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductInShoppingCartLowQuantityInWarehouse("Товар " + productId + " отсутствует на складе"));
            if (product.getQuantity() < requiredQty) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточно товара " + productId + " на складе");
            }

            // Накапливаем данные для агрегированного ответа (без списания)
            totalWeight += product.getWeight() * requiredQty;
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requiredQty;
            if (product.isFragile()) {
                hasFragile = true;
            }
        }

        log.info("Корзина {} проверена. Вес: {}, объём: {}, хрупкое: {}",
                cartDto.getShoppingCartId(), totalWeight, totalVolume, hasFragile);
        return new BookedProductsDto(totalWeight, totalVolume, hasFragile);
    }

    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        log.info("Добавление {} единиц товара {} на склад", request.getQuantity(), request.getProductId());
        WarehouseProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар не зарегистрирован на складе"));
        long newQuantity = product.getQuantity() + request.getQuantity();
        product.setQuantity(newQuantity);
        updateQuantityState(product.getProductId(), newQuantity);
    }

    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        UUID orderId = request.getOrderId();
        Map<UUID, Long> products = request.getProducts();
        log.info("Сборка заказа {}: товары {}", orderId, products);

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;
        List<OrderBooking> bookings = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            long requiredQty = entry.getValue();

            WarehouseProduct product = productRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар " + productId + " не зарегистрирован на складе"));

            // Резервируем (уменьшаем остаток)
            product.setQuantity(product.getQuantity() - requiredQty);
            updateQuantityState(productId, product.getQuantity());

            // Создаём запись бронирования
            OrderBooking booking = OrderBookingMapper.toEntity(orderId, productId, requiredQty);
            bookings.add(booking);

            // Агрегируем данные для ответа
            totalWeight += product.getWeight() * requiredQty;
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requiredQty;
            if (product.isFragile()) {
                hasFragile = true;
            }
        }

        bookingRepository.saveAll(bookings);
        log.info("Заказ {} собран. Зарезервировано {} позиций. Вес: {}, объём: {}, хрупкое: {}",
                orderId, bookings.size(), totalWeight, totalVolume, hasFragile);

        return new BookedProductsDto(totalWeight, totalVolume, hasFragile);
    }

    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        UUID orderId = request.getOrderId();
        UUID deliveryId = request.getDeliveryId();
        log.info("Передача заказа {} в доставку {}", orderId, deliveryId);

        // Проверяем, существуют ли записи бронирования для этого заказа
        List<OrderBooking> bookings = bookingRepository.findByOrderId(orderId);
        if (bookings.isEmpty()) {
            throw new BookingNotFoundException("Не найдены забронированные товары для заказа " + orderId);
        }

        // Обновляем deliveryId для всех записей этого заказа
        bookingRepository.updateDeliveryIdByOrderId(orderId, deliveryId);
        log.info("Для заказа {} установлен идентификатор доставки {}", orderId, deliveryId);
    }

    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        log.info("Возврат товаров на склад: {}", products);
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            long quantityToReturn = entry.getValue();

            WarehouseProduct product = productRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товар " + productId + " не зарегистрирован на складе"));
            long newQuantity = product.getQuantity() + quantityToReturn;
            product.setQuantity(newQuantity);
            updateQuantityState(productId, newQuantity);
        }
        log.info("Возврат завершён");
    }

    private void updateQuantityState(UUID productId, long quantity) {
        QuantityState state;
        if (quantity == 0) {
            state = QuantityState.ENDED;
        } else if (quantity < fewThreshold) {
            state = QuantityState.FEW;
        } else if (quantity <= enoughThreshold) {
            state = QuantityState.ENOUGH;
        } else {
            state = QuantityState.MANY;
        }
        try {
            SetProductQuantityStateRequest request = new SetProductQuantityStateRequest(productId, state);
            storeClient.setProductQuantityState(request.getProductId(), request.getQuantityState());
            log.info("Обновлён статус количества товара {}: {}", productId, state);
        } catch (Exception e) {
            log.info("Ошибка при обновлении статуса количества товара {}: {}", productId, state);
        }
    }
}