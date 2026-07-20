package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.PageProductDto;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.feign.fallback.ShoppingStoreClientFallback;

import java.util.UUID;
@FeignClient(name = "shopping-store", fallback = ShoppingStoreClientFallback.class,
        configuration = FeignConfig.class)
public interface ShoppingStoreClient {

    @GetMapping("/api/v1/shopping-store")
    PageProductDto getProducts(@RequestParam("category") ProductCategory category,
                               @RequestParam("page") int page,
                               @RequestParam("size") int size,
                               @RequestParam("sort") String[] sort);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createNewProduct(@RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    boolean setProductQuantityState(@RequestBody SetProductQuantityStateRequest request);
}