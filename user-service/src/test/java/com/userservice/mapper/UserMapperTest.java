package com.userservice.mapper;

import com.userservice.dto.UserCreateDto;
import com.userservice.dto.UserUpdateDto;
import com.userservice.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper unit tests")
class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    @DisplayName("toDto should return null when entity is null")
    void toDtoShouldReturnNullWhenEntityNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    @DisplayName("toDto should map basic fields")
    void toDtoShouldMapFields() {
        User user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setAge(27);

        var dto = mapper.toDto(user);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getAge(), dto.getAge());
    }

    @Test
    @DisplayName("toEntity should return null when create dto is null")
    void toEntityShouldReturnNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    @DisplayName("toEntity should map create dto fields to entity")
    void toEntityShouldMapFields() {
        UserCreateDto createDto = new UserCreateDto("Bob", "bob@example.com", 33);

        User user = mapper.toEntity(createDto);

        assertNotNull(user);
        assertEquals(createDto.getName(), user.getName());
        assertEquals(createDto.getEmail(), user.getEmail());
        assertEquals(createDto.getAge(), user.getAge());
    }

    @Test
    @DisplayName("updateEntityFromDto should ignore null update dto")
    void updateEntityShouldIgnoreNullDto() {
        User user = new User();
        user.setName("Initial");

        mapper.updateEntityFromDto(null, user);

        assertEquals("Initial", user.getName());
    }

    @Test
    @DisplayName("updateEntityFromDto should update only non-null fields")
    void updateEntityShouldUpdateNonNullFields() {
        User user = new User();
        user.setName("Old");
        user.setEmail("old@example.com");
        user.setAge(20);

        UserUpdateDto updateDto = new UserUpdateDto("New", "new@example.com", 30);

        mapper.updateEntityFromDto(updateDto, user);

        assertEquals("New", user.getName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals(30, user.getAge());
    }

    @Test
    @DisplayName("updateEntityFromDto should keep original values when dto fields are null")
    void updateEntityShouldKeepOriginalWhenDtoFieldsNull() {
        User user = new User();
        user.setName("Stable");
        user.setEmail("stable@example.com");
        user.setAge(27);

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName(null);
        updateDto.setEmail(null);
        updateDto.setAge(null);

        mapper.updateEntityFromDto(updateDto, user);

        assertEquals("Stable", user.getName());
        assertEquals("stable@example.com", user.getEmail());
        assertEquals(27, user.getAge());
    }
}

