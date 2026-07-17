package ru.yandex.practicum.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.ProductNotFoundException;

import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // Обрабатываем только ответы с ошибками 4xx
        if (response.status() == HttpStatus.NOT_FOUND.value()) {
            try (InputStream body = response.body().asInputStream()) {
                // Пытаемся прочитать тело как ProductNotFoundException
                String message = objectMapper.readTree(body).get("message").asText();
                return new ProductNotFoundException(message != null ? message : "Товар не найден");
            } catch (IOException e) {
                log.warn("Не удалось разобрать тело ошибки 404 от {}: {}", methodKey, e.getMessage());
            }
        } else if (response.status() == HttpStatus.BAD_REQUEST.value()) {
            try (InputStream body = response.body().asInputStream()) {
                String message = objectMapper.readTree(body).get("message").asText();
                return new ProductInShoppingCartLowQuantityInWarehouse(message != null ? message : "Ошибка склада");
            } catch (IOException e) {
                log.warn("Не удалось разобрать тело ошибки 400 от {}: {}", methodKey, e.getMessage());
            }
        }
        // Для остальных ошибок используем стандартный декодер
        return defaultErrorDecoder.decode(methodKey, response);
    }
}