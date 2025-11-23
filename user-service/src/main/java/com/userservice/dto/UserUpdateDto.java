package com.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Данные для обновления пользователя (все поля опциональны)")
public class UserUpdateDto {

    @Schema(description = "Имя пользователя", example = "Иван Иванов", required = false)
    @Size(min = 2, max = 30, message = "Имя должно содержать от 2 до 30 символов")
    private String name;

    @Schema(description = "Email пользователя", example = "user@example.com", required = false)
    @Email(message = "Некорректный формат Email")
    @Size(min = 5, max = 50, message = "Email должен содержать от 5 до 30 символов")
    private String email;

    @Schema(description = "Возраст пользователя", example = "25", required = false)
    @Min(value = 0, message = "Возраст не может быть отрицательным")
    @Max(value = 110, message = "Возраст не может быть больше 110")
    private Integer age;

    public UserUpdateDto(){
    }

    public UserUpdateDto(String name, String email, Integer age){
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}