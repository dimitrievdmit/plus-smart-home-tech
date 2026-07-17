package ru.yandex.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController {

    private final ShoppingStoreService service;
    private final ObjectMapper objectMapper;

    @GetMapping
    public PageProductDto getProducts(
            @RequestParam("category") ProductCategory category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) String[] sort) {
        return service.getProducts(category, page, size, sort != null ? sort : new String[0]);
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
    public boolean setProductQuantityState(HttpServletRequest request) throws IOException {
        if (request.getContentLengthLong() > 0
                && request.getContentType() != null
                && request.getContentType().contains("application/json")) {
            SetProductQuantityStateRequest req =
                    objectMapper.readValue(request.getInputStream(), SetProductQuantityStateRequest.class);
            return service.setProductQuantityState(req.getProductId(), req.getQuantityState());
        } else {
            // Поддержка query-параметров для совместимости с тестами
            UUID productId = UUID.fromString(request.getParameter("productId"));
            QuantityState state = QuantityState.valueOf(request.getParameter("quantityState"));
            return service.setProductQuantityState(productId, state);
        }
    }
}