package com.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.dto.UserCreateDto;
import com.userservice.dto.UserDto;
import com.userservice.dto.UserUpdateDto;
import com.userservice.exception.UserAlreadyExistsException;
import com.userservice.exception.UserNotFoundException;
import com.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@DisplayName("UserController API Test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    private UserDto testUserDto;
    private UserCreateDto testCreateDto;

    @BeforeEach
    void setUp(){
        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setName("TestName");
        testUserDto.setEmail("test@email.com");
        testUserDto.setAge(22);
        testUserDto.setCreatedAt(LocalDateTime.now());

        testCreateDto = new UserCreateDto();
        testCreateDto.setName("TestName");
        testCreateDto.setEmail("test@email.com");
        testCreateDto.setAge(22);
    }

    void shouldCreateUser() throws Exception{
        when(userService.createUser(any(UserCreateDto.class))).thenReturn(testUserDto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService, times(1)).createUser(any(UserCreateDto.class));
    }

    @Test
    @DisplayName("POST /api/users - должен вернуть 400 при невалидных данных")
    void shouldReturn400WhenInvalidData() throws Exception {
        // Arrange
        UserCreateDto invalidDto = new UserCreateDto();
        invalidDto.setName("A"); // Слишком короткое
        invalidDto.setEmail("invalid"); // Невалидный email
        invalidDto.setAge(-5); // Отрицательный

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    @DisplayName("GET /api/users/{id} - должен вернуть пользователя")
    void shouldGetUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUserDto);

        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - должен вернуть 404 если не найден")
    void shouldReturn404WhenNotFound() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/api/users/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users - должен вернуть всех пользователей")
    void shouldGetAllUsers() throws Exception {
        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setName("Jane Doe");
        user2.setEmail("jane@example.com");

        List<UserDto> users = Arrays.asList(testUserDto, user2);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - должен обновить пользователя")
    void shouldUpdateUser() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("John Smith");
        updateDto.setAge(31);

        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setName("John Smith");
        updatedUser.setEmail("john@example.com");
        updatedUser.setAge(31);

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Smith"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - должен удалить пользователя")
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}