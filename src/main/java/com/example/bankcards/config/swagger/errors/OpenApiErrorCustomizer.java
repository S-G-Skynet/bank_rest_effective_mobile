package com.example.bankcards.config.swagger.errors;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenApiErrorCustomizer {

    @Bean
    public OperationCustomizer commonApiErrorsCustomizer() {
        return (operation, handlerMethod) -> {

            CommonApiErrors errors = findAnnotation(handlerMethod);
            if (errors == null) {
                return operation;
            }

            if (errors.badRequest()) {
                add(operation, "400",
                        "Некорректный запрос",
                        "Bad request");
            }
            if (errors.unauthorized()) {
                add(operation, "401",
                        "Неавторизован",
                        "Authentication required");
            }
            if (errors.forbidden()) {
                add(operation, "403",
                        "Доступ запрещён",
                        "Access denied, please check your token");
            }
            if (errors.cardNotFound()) {
                add(operation, "404",
                        "Карта не найдена",
                        "Card not found with id: 0");
            }
            if (errors.userNotFound()) {
                add(operation, "404",
                        "Пользователь не найден",
                        "User not found with id or username");
            }
            if (errors.methodNotAllowed()) {
                add(operation, "405",
                        "Недостаточно средств на карте для перевода",
                        "You don't have that much money in your balance.");
            }
            if (errors.cardConflict()) {
                add(operation, "409",
                        "Карта с таким номером уже создана",
                        "Card already exists");
            }
            if (errors.userConflict()) {
                add(operation, "409",
                        "Пользователь с таким именем уже создан",
                        "User already exists");
            }
            if (errors.internalServerError()) {
                add(operation, "500",
                        "Внутренняя ошибка сервера",
                        "Unexpected error occurred");
            }

            return operation;
        };
    }

    private void add(io.swagger.v3.oas.models.Operation operation,
                     String code,
                     String description,
                     String message) {

        operation.getResponses().addApiResponse(
                code,
                errorResponse(Integer.parseInt(code), description, message)
        );
    }

    private CommonApiErrors findAnnotation(HandlerMethod handlerMethod) {
        CommonApiErrors method = handlerMethod.getMethodAnnotation(CommonApiErrors.class);
        if (method != null) {
            return method;
        }
        return handlerMethod.getBeanType().getAnnotation(CommonApiErrors.class);
    }

    private ApiResponse errorResponse(
            int code,
            String description,
            String message
    ) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("status", code);
        example.put("error", HttpStatus.valueOf(code).getReasonPhrase());
        example.put("message", message);
        example.put("timestamp", "2026-01-01T12:34:56");
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        "application/json",
                        new MediaType()
                                .example(example)
                ));
    }

}
