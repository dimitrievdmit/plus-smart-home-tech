package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ProductRepository;
import ru.yandex.practicum.util.SortParser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreService {
    private final ProductRepository productRepository;

    public ProductDto createProduct(ProductDto dto) {
        log.info("Создание нового товара: {}", dto.getProductName());
        Product product = ProductMapper.toEntity(dto);
        product.setProductState(dto.getProductState() != null ? dto.getProductState() : ProductState.ACTIVE);
        product.setQuantityState(dto.getQuantityState() != null ? dto.getQuantityState() : QuantityState.ENDED);
        product = productRepository.save(product);
        log.info("Товар создан с id: {}", product.getProductId());
        return ProductMapper.toDto(product);
    }

    public ProductDto getProduct(UUID productId) {
        log.info("Запрос товара по id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден"));
        return ProductMapper.toDto(product);
    }

    public PageProductDto getProducts(ProductCategory category, int page, int size, String[] sort) {
        log.info("Запрос страницы товаров: категория={}, страница={}, размер={}", category, page, size);
        Pageable pageable = PageRequest.of(page, size, SortParser.parseSort(sort));
        Page<Product> productPage = productRepository.findAllByProductCategory(category, pageable);
        return ProductMapper.toPageProductDto(productPage);
    }

    public ProductDto updateProduct(ProductDto dto) {
        log.info("Обновление товара с id: {}", dto.getProductId());
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден"));
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setImageSrc(dto.getImageSrc());
        product.setProductCategory(dto.getProductCategory());
        product.setPrice(dto.getPrice());
        product.setProductState(dto.getProductState() != null ? dto.getProductState() : product.getProductState());
        product.setQuantityState(dto.getQuantityState() != null ? dto.getQuantityState() : product.getQuantityState());
        product = productRepository.save(product);
        log.info("Товар обновлён: {}", product.getProductId());
        return ProductMapper.toDto(product);
    }

    public boolean removeProductFromStore(UUID productId) {
        log.info("Деактивация товара с id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден"));
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        // обработка варианта по спецификации.
        return setProductQuantityState(request.getProductId(), request.getQuantityState());
    }

    public boolean setProductQuantityState(UUID productId, QuantityState quantityState) {
        log.info("Изменение статуса количества товара {} на {}", productId, quantityState);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден"));
        product.setQuantityState(quantityState);
        productRepository.save(product);
        return true;
    }

    private Sort parseSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        String pendingProperty = null;

        for (String token : sort) {
            if (token == null || token.isBlank()) {
                continue;
            }
            String trimmed = token.trim();
            String lower = trimmed.toLowerCase();

            if ("asc".equals(lower) || "desc".equals(lower)) {
                // Это направление – применяем к предыдущему свойству
                if (pendingProperty != null) {
                    Sort.Direction direction = "desc".equals(lower) ? Sort.Direction.DESC : Sort.Direction.ASC;
                    orders.add(new Sort.Order(direction, pendingProperty));
                    pendingProperty = null;
                }
            } else {
                // Это новое свойство – если было незавершённое, фиксируем его с направлением по умолчанию
                if (pendingProperty != null) {
                    orders.add(new Sort.Order(Sort.Direction.ASC, pendingProperty));
                }
                pendingProperty = trimmed;
            }
        }
        // Обрабатываем последнее ожидающее свойство
        if (pendingProperty != null) {
            orders.add(new Sort.Order(Sort.Direction.ASC, pendingProperty));
        }

        log.info("Разобранные параметры сортировки: {}", orders);
        return Sort.by(orders);
    }
}