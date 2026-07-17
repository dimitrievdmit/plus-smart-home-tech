package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.AddressService;
import ru.yandex.practicum.service.WarehouseService;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;
    private final AddressService addressService;

    @PutMapping
    public void newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request) {
        warehouseService.addNewProduct(request);
    }

    @PostMapping("/check")
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto cartDto) {
        return warehouseService.checkCart(cartDto);
    }

    @PostMapping("/add")
    public void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request) {
        warehouseService.addProductQuantity(request);
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        return addressService.getAddress();
    }
}