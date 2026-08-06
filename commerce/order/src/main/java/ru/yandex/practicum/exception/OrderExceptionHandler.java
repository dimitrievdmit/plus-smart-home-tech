package ru.yandex.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(NotAuthorizedUserException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleServiceUnavailable(ServiceUnavailableException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler({
            NoOrderFoundException.class,
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(Exception ex) {
        String message;
        if (ex instanceof MethodArgumentNotValidException) {
            message = ((MethodArgumentNotValidException) ex).getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        } else if (ex instanceof HttpMessageNotReadableException) {
            message = "Malformed JSON request: " + ((HttpMessageNotReadableException) ex).getMostSpecificCause().getMessage();
        } else {
            message = ex.getMessage();
        }
        return new ErrorResponse(message);
    }
}