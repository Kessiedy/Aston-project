package com.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Запрос на создание пользоавтеля")
public class UserCreateDto {

    @Schema(description = "Имя пользователя", example = "Райан Гослинг", required = true)
    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 30, message = "Имя должно содержать от 2 до 30 символов")
    private String name;

    @Schema(description = "Email пользователя", example = "newuser@example.com", required = true)
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат Email")
    @Size(min = 5, max = 50, message = "Email должен содержать от 5 до 30 символов")
    private String email;

    @Schema(description = "Возраст пользователя", example = "52", required = true)
    @Min(value = 0, message = "Возраст не может быть отрицательным")
    @Max(value = 110, message = "Возраст не может быть больше 110")
    private Integer age;

    public UserCreateDto() {

    }

    public UserCreateDto(String name, String email, Integer age) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "UserCreateDto{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                '}';
    }
}