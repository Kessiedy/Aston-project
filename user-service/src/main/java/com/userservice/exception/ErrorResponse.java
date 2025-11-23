package com.userservice.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Стандартный ответ об ошибке")
public class ErrorResponse {

    @Schema(description = "Время возникновения ошибки", example = "2025-11-20T18:13:24", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP статус код", example = "404", accessMode = Schema.AccessMode.READ_ONLY)
    private int status;
    
    @Schema(description = "Тип ошибки", example = "Not Found", accessMode = Schema.AccessMode.READ_ONLY)
    private String error;
    
    @Schema(description = "Сообщение об ошибке", example = "Пользователь с ID 1 не найден", accessMode = Schema.AccessMode.READ_ONLY)
    private String message;

    public ErrorResponse(){
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
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
}