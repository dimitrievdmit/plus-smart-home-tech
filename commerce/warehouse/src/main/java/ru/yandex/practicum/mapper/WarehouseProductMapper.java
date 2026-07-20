package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.DimensionDto;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.WarehouseProduct;

public final class WarehouseProductMapper {

    public static WarehouseProduct toEntity(NewProductInWarehouseRequest request) {
        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(request.getProductId());
        product.setFragile(request.isFragile());
        DimensionDto dim = request.getDimension();
        product.setWidth(dim.getWidth());
        product.setHeight(dim.getHeight());
        product.setDepth(dim.getDepth());
        product.setWeight(request.getWeight());
        product.setQuantity(0);  // новый товар всегда без остатка
        return product;
    }
}