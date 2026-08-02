package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.ShoppingStoreApi;
import ru.yandex.practicum.dto.PageProductDto;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.QuantityState;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreApi {

    private final ShoppingStoreService service;

    @Override
    public PageProductDto getProducts(
            @RequestParam("category") ProductCategory category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) String[] sort) {
        return service.getProducts(category, page, size, sort != null ? sort : new String[0]);
    }

    @Override
    public ProductDto getProduct(@PathVariable UUID productId) {
        return service.getProduct(productId);
    }

    @Override
    public ProductDto createNewProduct(@RequestBody ProductDto productDto) {
        return service.createProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        return service.updateProduct(productDto);
    }

    @Override
    public boolean removeProductFromStore(@RequestBody UUID productId) {
        return service.removeProductFromStore(productId);
    }

//    @Override
//    public boolean setProductQuantityState(@RequestBody SetProductQuantityStateRequest request) {
//        // обработка варианта по спецификации.
//        return service.setProductQuantityState(request);
//    }

    @Override
    public boolean setProductQuantityState(@RequestParam UUID productId,
                                           @RequestParam QuantityState quantityState) {
        // обработка варианта c query параметрами, которые отправляют тесты.
        return service.setProductQuantityState(productId, quantityState);
    }
}