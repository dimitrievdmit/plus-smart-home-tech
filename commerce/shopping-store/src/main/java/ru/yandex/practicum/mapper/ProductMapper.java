package ru.yandex.practicum.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.PageProductDto;
import ru.yandex.practicum.dto.PageableObject;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SortObject;
import ru.yandex.practicum.model.Product;

import java.util.List;

public final class ProductMapper {

    public static ProductDto toDto(Product product) {
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

    public static Product toEntity(ProductDto dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setImageSrc(dto.getImageSrc());
        product.setProductCategory(dto.getProductCategory());
        product.setPrice(dto.getPrice());
        // Поля quantityState и productState устанавливаются в сервисе
        return product;
    }

    public static PageProductDto toPageProductDto(Page<Product> productPage) {
        PageProductDto result = new PageProductDto();

        // Контент
        List<ProductDto> content = productPage.getContent().stream()
                .map(ProductMapper::toDto)
                .toList();
        result.setContent(content);
        result.setTotalElements(productPage.getTotalElements());
        result.setTotalPages(productPage.getTotalPages());
        result.setFirst(productPage.isFirst());
        result.setLast(productPage.isLast());
        result.setSize(productPage.getSize());
        result.setNumber(productPage.getNumber());
        result.setNumberOfElements(productPage.getNumberOfElements());
        result.setEmpty(productPage.isEmpty());

        // Сортировка
        List<SortObject> sortObjects = productPage.getSort().stream()
                .map(order -> new SortObject(
                        order.getDirection().name(),
                        order.getNullHandling().name(),
                        order.isAscending(),
                        order.getProperty(),
                        order.isIgnoreCase()))
                .toList();
        result.setSort(sortObjects);

        // Информация о пагинации
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
}