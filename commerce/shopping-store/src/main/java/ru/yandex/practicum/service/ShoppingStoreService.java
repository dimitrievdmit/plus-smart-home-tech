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
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ProductRepository;

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
        Product product = mapToEntity(dto);
        product.setProductState(dto.getProductState() != null ? dto.getProductState() : ProductState.ACTIVE);
        product.setQuantityState(dto.getQuantityState() != null ? dto.getQuantityState() : QuantityState.ENDED);
        product = productRepository.save(product);
        log.info("Товар создан с id: {}", product.getProductId());
        return mapToDto(product);
    }

    public ProductDto getProduct(UUID productId) {
        log.info("Запрос товара по id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден"));
        return mapToDto(product);
    }

    public PageProductDto getProducts(ProductCategory category, int page, int size, String[] sort) {
        log.info("Запрос страницы товаров: категория={}, страница={}, размер={}", category, page, size);
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<Product> productPage = productRepository.findAllByProductStateAndProductCategory(ProductState.ACTIVE, category, pageable);

        PageProductDto result = new PageProductDto();
        result.setContent(productPage.getContent().stream().map(this::mapToDto).toList());
        result.setTotalElements(productPage.getTotalElements());
        result.setTotalPages(productPage.getTotalPages());
        result.setFirst(productPage.isFirst());
        result.setLast(productPage.isLast());
        result.setSize(productPage.getSize());
        result.setNumber(productPage.getNumber());
        result.setNumberOfElements(productPage.getNumberOfElements());
        result.setEmpty(productPage.isEmpty());

        // Заполнение критериев сортировки
        List<SortObject> sortObjects = productPage.getSort().stream()
                .map(order -> new SortObject(
                        order.getDirection().name(),
                        order.getNullHandling().name(),
                        order.isAscending(),
                        order.getProperty(),
                        order.isIgnoreCase()))
                .toList();
        result.setSort(sortObjects);

        // Заполнение PageableObject
        Pageable pageableInfo = productPage.getPageable();
        result.setPageable(new PageableObject(
                pageableInfo.getOffset(),
                sortObjects,
                pageableInfo.isUnpaged(),
                pageableInfo.isPaged(),
                pageableInfo.getPageNumber(),
                pageableInfo.getPageSize()));

        return result;
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
        return mapToDto(product);
    }

    public boolean removeProductFromStore(UUID productId) {
        log.info("Деактивация товара с id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден"));
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    public boolean setProductQuantityState(UUID productId, QuantityState quantityState) {
        log.info("Изменение статуса количества товара {} на {}", productId, quantityState);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден"));
        product.setQuantityState(quantityState);
        productRepository.save(product);
        return true;
    }

    private Product mapToEntity(ProductDto dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setImageSrc(dto.getImageSrc());
        product.setProductCategory(dto.getProductCategory());
        product.setPrice(dto.getPrice());
        return product;
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setImageSrc(product.getImageSrc());
        dto.setQuantityState(product.getQuantityState());
        dto.setProductState(product.getProductState());
        dto.setProductCategory(product.getProductCategory());
        dto.setPrice(product.getPrice());
        return dto;
    }

    private Sort parseSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String sortParam : sort) {
            if (sortParam == null || sortParam.isBlank()) {
                continue;
            }
            String[] parts = sortParam.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = Sort.Direction.ASC;
            if (parts.length > 1) {
                String dir = parts[1].trim().toLowerCase();
                if ("desc".equals(dir)) {
                    direction = Sort.Direction.DESC;
                }
            }
            orders.add(new Sort.Order(direction, property));
        }

        log.info("Разобранные параметры сортировки: {}", orders);
        return Sort.by(orders);
    }
}