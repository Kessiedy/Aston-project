package com.userservice.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Ответ об ошибке валидации с деталями по полям")
public class ValidationErrorResponse {

    @Schema(description = "Время возникновения ошибки", example = "2025-11-20T18:13:24", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP статус код", example = "400", accessMode = Schema.AccessMode.READ_ONLY)
    private int status;
    
    @Schema(description = "Тип ошибки", example = "Validation Failed", accessMode = Schema.AccessMode.READ_ONLY)
    private String error;
    
    @Schema(description = "Общее сообщение об ошибке", example = "Некорректные данные", accessMode = Schema.AccessMode.READ_ONLY)
    private String message;
    
    @Schema(description = "Детали ошибок валидации по полям", example = "{\"email\": \"Некорректный формат Email\", \"name\": \"Имя не должно быть пустым\"}", accessMode = Schema.AccessMode.READ_ONLY)
    private Map<String, String> fieldErrors;

    public ValidationErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ValidationErrorResponse(int status, String error, String message, Map<String, String> fieldErrors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}