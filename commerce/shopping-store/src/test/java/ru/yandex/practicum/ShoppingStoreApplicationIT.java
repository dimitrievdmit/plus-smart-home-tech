package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.controller.ShoppingStoreController;
import ru.yandex.practicum.dto.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingStoreApplicationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationContext context;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/shopping-store";
    }

    @Test
    void contextLoads() {
        // Проверка, что контроллер зарегистрирован
        assertThat(context.getBean(ShoppingStoreController.class)).isNotNull();

        // Вывод всех маппингов для диагностики
        RequestMappingHandlerMapping mapping = context.getBean(RequestMappingHandlerMapping.class);
        mapping.getHandlerMethods().forEach((key, value) -> {
            System.out.println(key + " -> " + value);
        });

        // Проверка, что бин контроллера существует
        Map<String, Object> controllers = context.getBeansWithAnnotation(RestController.class);
        System.out.println("Найденные RestController бины: " + controllers.keySet());

        assertThat(context.getBeanNamesForType(ShoppingStoreController.class)).isNotEmpty();
    }

    @Test
    void shouldCreateProduct() {
        ProductDto newProduct = new ProductDto();
        newProduct.setProductName("Светодиодная лента");
        newProduct.setDescription("Управляемая светодиодная лента");
        newProduct.setPrice(1299.99);
        newProduct.setProductCategory(ProductCategory.LIGHTING);
        newProduct.setProductState(ProductState.ACTIVE);
        newProduct.setQuantityState(QuantityState.ENOUGH);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                new HttpEntity<>(newProduct),
                ProductDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductDto created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getProductId()).isNotNull();
        assertThat(created.getProductName()).isEqualTo("Светодиодная лента");
        assertThat(created.getProductState()).isEqualTo(ProductState.ACTIVE);
    }

    @Test
    void shouldGetProductById() {
        // Создаём товар, затем получаем по ID
        ProductDto newProduct = new ProductDto();
        newProduct.setProductName("Тестовый датчик");
        newProduct.setDescription("Датчик движения");
        newProduct.setPrice(500);
        newProduct.setProductCategory(ProductCategory.SENSORS);
        newProduct.setProductState(ProductState.ACTIVE);
        newProduct.setQuantityState(QuantityState.FEW);

        ResponseEntity<ProductDto> createResp = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                new HttpEntity<>(newProduct),
                ProductDto.class
        );
        UUID id = createResp.getBody().getProductId();

        ResponseEntity<ProductDto> getResp = restTemplate.getForEntity(
                baseUrl + "/" + id,
                ProductDto.class
        );
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().getProductName()).isEqualTo("Тестовый датчик");
    }

    @Test
    void shouldReturn404ForUnknownProduct() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/" + UUID.randomUUID(),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateProduct() {
        // Создаём, потом обновляем
        ProductDto newProduct = new ProductDto();
        newProduct.setProductName("Старое имя");
        newProduct.setDescription("Старое описание");
        newProduct.setPrice(100);
        newProduct.setProductCategory(ProductCategory.CONTROL);
        newProduct.setProductState(ProductState.ACTIVE);
        newProduct.setQuantityState(QuantityState.ENOUGH);

        ProductDto created = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                new HttpEntity<>(newProduct),
                ProductDto.class
        ).getBody();

        created.setProductName("Новое имя");
        created.setDescription("Новое описание");
        created.setPrice(200);
        created.setProductCategory(ProductCategory.LIGHTING);

        ResponseEntity<ProductDto> updateResp = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(created),
                ProductDto.class
        );

        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResp.getBody().getProductName()).isEqualTo("Новое имя");
        assertThat(updateResp.getBody().getDescription()).isEqualTo("Новое описание");
        assertThat(updateResp.getBody().getPrice()).isEqualTo(200.0);
        assertThat(updateResp.getBody().getProductCategory()).isEqualTo(ProductCategory.LIGHTING);
    }

    @Test
    void shouldDeactivateProduct() {
        ProductDto newProduct = new ProductDto();
        newProduct.setProductName("Удаляемый");
        newProduct.setDescription("...");
        newProduct.setPrice(1);
        newProduct.setProductCategory(ProductCategory.CONTROL);
        newProduct.setProductState(ProductState.ACTIVE);
        newProduct.setQuantityState(QuantityState.ENDED);

        ProductDto created = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                new HttpEntity<>(newProduct),
                ProductDto.class
        ).getBody();

        ResponseEntity<Boolean> removeResp = restTemplate.exchange(
                baseUrl + "/removeProductFromStore",
                HttpMethod.POST,
                new HttpEntity<>(created.getProductId()),
                Boolean.class
        );
        assertThat(removeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(removeResp.getBody()).isTrue();

        // Проверим, что состояние изменилось
        ResponseEntity<ProductDto> getResp = restTemplate.getForEntity(
                baseUrl + "/" + created.getProductId(),
                ProductDto.class
        );
        assertThat(getResp.getBody().getProductState()).isEqualTo(ProductState.DEACTIVATE);
    }

    @Test
    void shouldSetQuantityState() {
        ProductDto newProduct = new ProductDto();
        newProduct.setProductName("Количественный");
        newProduct.setDescription("...");
        newProduct.setPrice(1);
        newProduct.setProductCategory(ProductCategory.SENSORS);
        newProduct.setProductState(ProductState.ACTIVE);
        newProduct.setQuantityState(QuantityState.ENOUGH);

        ProductDto created = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                new HttpEntity<>(newProduct),
                ProductDto.class
        ).getBody();

        SetProductQuantityStateRequest request = new SetProductQuantityStateRequest(
                created.getProductId(), QuantityState.FEW);

        ResponseEntity<Boolean> setResp = restTemplate.exchange(
                baseUrl + "/quantityState",
                HttpMethod.POST,
                new HttpEntity<>(request),
                Boolean.class
        );
        assertThat(setResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(setResp.getBody()).isTrue();

        // Проверяем изменение
        ResponseEntity<ProductDto> getResp = restTemplate.getForEntity(
                baseUrl + "/" + created.getProductId(),
                ProductDto.class
        );
        assertThat(getResp.getBody().getQuantityState()).isEqualTo(QuantityState.FEW);
    }

    @Test
    void shouldGetProductsByCategory() {
        // Создадим пару товаров в категории LIGHTING
        ProductDto p1 = new ProductDto();
        p1.setProductName("Лампа");
        p1.setDescription("...");
        p1.setPrice(100);
        p1.setProductCategory(ProductCategory.LIGHTING);
        p1.setProductState(ProductState.ACTIVE);
        p1.setQuantityState(QuantityState.MANY);
        restTemplate.put(baseUrl, p1);

        ProductDto p2 = new ProductDto();
        p2.setProductName("Лента");
        p2.setDescription("...");
        p2.setPrice(50);
        p2.setProductCategory(ProductCategory.LIGHTING);
        p2.setProductState(ProductState.ACTIVE);
        p2.setQuantityState(QuantityState.ENOUGH);
        restTemplate.put(baseUrl, p2);

        ResponseEntity<PageProductDto> response = restTemplate.getForEntity(
                UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .queryParam("category", "LIGHTING")
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .queryParam("sort", "productName,asc")
                        .build()
                        .toUriString(),
                PageProductDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageProductDto page = response.getBody();
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().size()).isGreaterThanOrEqualTo(2);
        // Проверим, что оба товара в категории LIGHTING
        page.getContent().forEach(p -> assertThat(p.getProductCategory()).isEqualTo(ProductCategory.LIGHTING));
    }
}