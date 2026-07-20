package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.feign.WarehouseClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartApplicationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private WarehouseClient warehouseClient;   // замокали склад

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/shopping-cart";
        // Возвращаем заглушку BookedProductsDto для любых проверок склада
        when(warehouseClient.checkProductQuantityEnoughForShoppingCart(any(ShoppingCartDto.class)))
                .thenReturn(new BookedProductsDto(1.0, 2.0, false));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldGetOrCreateCart() {
        String username = "testUser";
        ResponseEntity<ShoppingCartDto> response = restTemplate.getForEntity(
                baseUrl + "?username=" + username,
                ShoppingCartDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ShoppingCartDto cart = response.getBody();
        assertThat(cart.getShoppingCartId()).isNotNull();
        assertThat(cart.getProducts()).isEmpty();
    }

    @Test
    void shouldAddProductToCart() {
        String username = "testUser2";
        Map<UUID, Long> products = new HashMap<>();
        UUID productId = UUID.randomUUID();
        products.put(productId, 2L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ShoppingCartDto> response = restTemplate.exchange(
                baseUrl + "?username=" + username,
                HttpMethod.PUT,
                new HttpEntity<>(products, headers),
                ShoppingCartDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ShoppingCartDto cart = response.getBody();
        assertThat(cart.getProducts()).containsKey(productId);
        assertThat(cart.getProducts().get(productId)).isEqualTo(2L);
    }

    @Test
    void shouldChangeProductQuantity() {
        String username = "testUser3";
        UUID productId = UUID.randomUUID();
        // Сначала добавляем
        Map<UUID, Long> initial = Map.of(productId, 1L);
        restTemplate.put(baseUrl + "?username=" + username, initial);

        ChangeProductQuantityRequest changeRequest = new ChangeProductQuantityRequest(productId, 5L);
        ResponseEntity<ShoppingCartDto> response = restTemplate.exchange(
                baseUrl + "/change-quantity?username=" + username,
                HttpMethod.POST,
                new HttpEntity<>(changeRequest),
                ShoppingCartDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ShoppingCartDto cart = response.getBody();
        assertThat(cart.getProducts().get(productId)).isEqualTo(5L);
    }

    @Test
    void shouldRemoveProductFromCart() {
        String username = "testUser4";
        UUID productId = UUID.randomUUID();
        restTemplate.put(baseUrl + "?username=" + username, Map.of(productId, 1L));

        List<UUID> toRemove = List.of(productId);
        ResponseEntity<ShoppingCartDto> response = restTemplate.exchange(
                baseUrl + "/remove?username=" + username,
                HttpMethod.POST,
                new HttpEntity<>(toRemove),
                ShoppingCartDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ShoppingCartDto cart = response.getBody();
        assertThat(cart.getProducts()).doesNotContainKey(productId);
    }

    @Test
    void shouldDeactivateCart() {
        String username = "testUser5";
        // Создаём корзину
        restTemplate.getForEntity(baseUrl + "?username=" + username, ShoppingCartDto.class);

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "?username=" + username,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldGetDeactivatedCartsHistory() {
        String username = "testUser6";
        // Создаём и деактивируем
        restTemplate.getForEntity(baseUrl + "?username=" + username, ShoppingCartDto.class);
        restTemplate.delete(baseUrl + "?username=" + username);

        // Запрашиваем историю
        ResponseEntity<ShoppingCartDto[]> response = restTemplate.getForEntity(
                baseUrl + "/history?username=" + username,
                ShoppingCartDto[].class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        // Все деактивированные
        // (проверки состояния здесь нет, но можно проверить, что список не пуст)
    }

    @Test
    void shouldReturn401ForEmptyUsername() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "?username=",
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}