package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.PageProductDto;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController {

    private final ShoppingStoreService service;

    @GetMapping
    public PageProductDto getProducts(@RequestParam("category") ProductCategory category,
                                      @RequestParam("page") int page,
                                      @RequestParam("size") int size,
                                      @RequestParam("sort") String[] sort) {
        return service.getProducts(category, page, size, sort);
    }

    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        return service.getProduct(productId);
    }

    @PutMapping
    public ProductDto createNewProduct(@RequestBody ProductDto productDto) {
        return service.createProduct(productDto);
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        return service.updateProduct(productDto);
    }

    @PostMapping("/removeProductFromStore")
    public boolean removeProductFromStore(@RequestBody UUID productId) {
        return service.removeProductFromStore(productId);
    }

    @PostMapping("/quantityState")
    public boolean setProductQuantityState(@RequestBody SetProductQuantityStateRequest request) {
        return service.setProductQuantityState(request.getProductId(), request.getQuantityState());
    }
}