package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.ShoppingStoreClient;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WarehouseApplicationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ShoppingStoreClient shoppingStoreClient;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/warehouse";
        // По умолчанию вызов setProductQuantityState ничего не делает (возвращаем true)
        when(shoppingStoreClient.setProductQuantityState(any())).thenReturn(true);
    }

    @Test
    void contextLoads() {}

    @Test
    void shouldAddNewProductToWarehouse() {
        UUID productId = UUID.randomUUID();
        NewProductInWarehouseRequest request = new NewProductInWarehouseRequest();
        request.setProductId(productId);
        request.setFragile(true);
        request.setWeight(2.5);
        DimensionDto dim = new DimensionDto(10, 5, 3);
        request.setDimension(dim);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldRejectDuplicateProduct() {
        UUID productId = UUID.randomUUID();
        NewProductInWarehouseRequest request = new NewProductInWarehouseRequest();
        request.setProductId(productId);
        request.setFragile(false);
        request.setWeight(1);
        request.setDimension(new DimensionDto(1,1,1));

        restTemplate.put(baseUrl, request);
        ResponseEntity<String> second = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                String.class
        );
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldAddProductQuantity() {
        UUID productId = UUID.randomUUID();
        // Сначала регистрируем
        NewProductInWarehouseRequest newReq = new NewProductInWarehouseRequest();
        newReq.setProductId(productId);
        newReq.setFragile(false);
        newReq.setWeight(1);
        newReq.setDimension(new DimensionDto(1,1,1));
        restTemplate.put(baseUrl, newReq);

        // Добавляем количество
        AddProductToWarehouseRequest addReq = new AddProductToWarehouseRequest(productId, 10);
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/add",
                HttpMethod.POST,
                new HttpEntity<>(addReq),
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldCheckShoppingCartAndReserve() {
        // Регистрируем товар с достаточным количеством
        UUID productId = UUID.randomUUID();
        NewProductInWarehouseRequest newReq = new NewProductInWarehouseRequest();
        newReq.setProductId(productId);
        newReq.setFragile(false);
        newReq.setWeight(0.5);
        newReq.setDimension(new DimensionDto(2,2,2));
        restTemplate.put(baseUrl, newReq);

        // Добавляем количество 5
        AddProductToWarehouseRequest addReq = new AddProductToWarehouseRequest(productId, 5);
        restTemplate.postForEntity(baseUrl + "/add", addReq, Void.class);

        // Формируем корзину
        ShoppingCartDto cartDto = new ShoppingCartDto();
        cartDto.setShoppingCartId(UUID.randomUUID());
        Map<UUID, Long> products = new HashMap<>();
        products.put(productId, 3L);
        cartDto.setProducts(products);

        ResponseEntity<BookedProductsDto> response = restTemplate.exchange(
                baseUrl + "/check",
                HttpMethod.POST,
                new HttpEntity<>(cartDto),
                BookedProductsDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookedProductsDto booked = response.getBody();
        assertThat(booked.getDeliveryWeight()).isGreaterThan(0);
        assertThat(booked.getDeliveryVolume()).isGreaterThan(0);
        assertThat(booked.isFragile()).isFalse();
    }

    @Test
    void shouldFailIfNotEnoughQuantity() {
        UUID productId = UUID.randomUUID();
        NewProductInWarehouseRequest newReq = new NewProductInWarehouseRequest();
        newReq.setProductId(productId);
        newReq.setFragile(false);
        newReq.setWeight(1);
        newReq.setDimension(new DimensionDto(1,1,1));
        restTemplate.put(baseUrl, newReq);
        // Количество не добавляем (0)

        ShoppingCartDto cartDto = new ShoppingCartDto();
        cartDto.setShoppingCartId(UUID.randomUUID());
        cartDto.setProducts(Map.of(productId, 1L));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/check",
                HttpMethod.POST,
                new HttpEntity<>(cartDto),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldGetWarehouseAddress() {
        ResponseEntity<AddressDto> response = restTemplate.getForEntity(
                baseUrl + "/address",
                AddressDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AddressDto address = response.getBody();
        assertThat(address.getCountry()).isNotNull();
        assertThat(address.getCity()).isNotNull();
    }
}