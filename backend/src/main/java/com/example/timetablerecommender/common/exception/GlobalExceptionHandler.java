package com.example.timetablerecommender.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.timetablerecommender.common.api.ApiError;
import com.example.timetablerecommender.common.api.ApiResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.failure(new ApiError(
                        exception.getCode(), exception.getMessage(), null)));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    ResponseEntity<ApiResponse<Void>> handleBodyValidation(BindException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return invalidRequest(fieldErrors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiResponse<Void>> handleMethodValidation(HandlerMethodValidationException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error ->
                    fieldErrors.putIfAbsent(name == null ? "request" : name, error.getDefaultMessage()));
        });
        return invalidRequest(fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiResponse<Void>> handleConstraintValidation(ConstraintViolationException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);
            fieldErrors.putIfAbsent(field, violation.getMessage());
        });
        return invalidRequest(fieldErrors);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        return invalidRequest(null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(new ApiError(
                        "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", null)));
    }

    private ResponseEntity<ApiResponse<Void>> invalidRequest(Map<String, String> fieldErrors) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(new ApiError(
                "INVALID_REQUEST", "요청 값이 올바르지 않습니다.", fieldErrors)));
    }
}
