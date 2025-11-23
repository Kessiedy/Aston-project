package com.userservice.controller;

import com.userservice.dto.UserCreateDto;
import com.userservice.dto.UserDto;
import com.userservice.dto.UserUpdateDto;
import com.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "API для управления пользователями")
public class UserController {

    private static final Logger log = LogManager.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Создать нового пользователя", description = "Создает нового пользователя с указанными данными")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные пользователя",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<UserDto>> createUser(
            @Parameter(description = "Данные для создания пользователя", required = true)
            @Valid @RequestBody UserCreateDto createDto){
        log.info("REST request to create user: {}", createDto.getEmail());
        UserDto createdUser = userService.createUser(createDto);
        EntityModel<UserDto> userModel = EntityModel.of(createdUser);
        userModel.add(linkTo(methodOn(UserController.class).getUserById(createdUser.getId())).withSelfRel());
        userModel.add(linkTo(methodOn(UserController.class).updateUser(createdUser.getId(), null)).withRel("update"));
        userModel.add(linkTo(methodOn(UserController.class).deleteUser(createdUser.getId())).withRel("delete"));
        userModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }

    @GetMapping("/count")
    @Operation(summary = "Получить количество пользователей", description = "Возвращает общее количество пользователей в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Количество пользователей успешно получено",
                    content = @Content(schema = @Schema(implementation = Long.class)))
    })
    public ResponseEntity<Long> getUserCount(){
        log.info("REST request to get user count");
        long count = userService.getUserCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/by-email")
    @Operation(summary = "Найти пользователя по email", description = "Возвращает пользователя с указанным email адресом")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<UserDto>> getUserByEmail(
            @Parameter(description = "Email адрес пользователя", required = true, example = "user@example.com")
            @RequestParam String email){
        log.info("REST request to get user by email: {}", email);
        UserDto user = userService.getUserByEmail(email);
        EntityModel<UserDto> userModel = EntityModel.of(user);
        userModel.add(linkTo(methodOn(UserController.class).getUserByEmail(email)).withSelfRel());
        userModel.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withRel("user"));
        userModel.add(linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"));
        userModel.add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
        userModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        return ResponseEntity.ok(userModel);
    }
    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя с указанным идентификатором")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<UserDto>> getUserById(
            @Parameter(description = "Идентификатор пользователя", required = true, example = "1")
            @PathVariable Long id){
        log.info("REST request to get user by ID: {}", id);
        UserDto user = userService.getUserById(id);
        EntityModel<UserDto> userModel = EntityModel.of(user);
        userModel.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        userModel.add(linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"));
        userModel.add(linkTo(methodOn(UserController.class).patchUser(id, null)).withRel("patch"));
        userModel.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        userModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        return ResponseEntity.ok(userModel);
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = UserDto.class)))
    })
    public ResponseEntity<CollectionModel<EntityModel<UserDto>>> getAllUsers(){
        log.info("REST request to get all users");
        List<UserDto> users = userService.getAllUsers();
        List<EntityModel<UserDto>> userModels = users.stream()
                .map(user -> {
                    EntityModel<UserDto> userModel = EntityModel.of(user);
                    userModel.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());
                    userModel.add(linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"));
                    userModel.add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
                    return userModel;
                })
                .collect(Collectors.toList());
        
        CollectionModel<EntityModel<UserDto>> collectionModel = CollectionModel.of(userModels);
        collectionModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
        collectionModel.add(linkTo(methodOn(UserController.class).createUser(null)).withRel("create"));
        collectionModel.add(linkTo(methodOn(UserController.class).getUserCount()).withRel("count"));
        return ResponseEntity.ok(collectionModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Полностью обновить пользователя", description = "Обновляет все поля пользователя с указанным идентификатором")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные пользователя",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email уже используется другим пользователем",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<UserDto>> updateUser(
            @Parameter(description = "Идентификатор пользователя", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Данные для обновления пользователя", required = true)
            @Valid @RequestBody UserUpdateDto updateDto){
        log.info("REST request to update user with ID: {}", id);
        UserDto updatedUser = userService.updateUser(id, updateDto);
        EntityModel<UserDto> userModel = EntityModel.of(updatedUser);
        userModel.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        userModel.add(linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"));
        userModel.add(linkTo(methodOn(UserController.class).patchUser(id, null)).withRel("patch"));
        userModel.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        userModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        return ResponseEntity.ok(userModel);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Частично обновить пользователя", description = "Обновляет указанные поля пользователя с указанным идентификатором")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные пользователя",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email уже используется другим пользователем",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<UserDto>> patchUser(
            @Parameter(description = "Идентификатор пользователя", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Данные для частичного обновления пользователя", required = true)
            @RequestBody UserUpdateDto updateDto){
        log.info("REST request to patch user with ID: {}", id);
        UserDto updatedUser = userService.updateUser(id, updateDto);
        EntityModel<UserDto> userModel = EntityModel.of(updatedUser);
        userModel.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        userModel.add(linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"));
        userModel.add(linkTo(methodOn(UserController.class).patchUser(id, null)).withRel("patch"));
        userModel.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        userModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        return ResponseEntity.ok(userModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя с указанным идентификатором")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = com.userservice.exception.ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Идентификатор пользователя", required = true, example = "1")
            @PathVariable Long id){
        log.info("REST request to delete user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}