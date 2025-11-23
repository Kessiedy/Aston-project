package com.userservice.service;

import com.userservice.dto.UserCreateDto;
import com.userservice.dto.UserDto;
import com.userservice.dto.UserUpdateDto;
import com.userservice.entity.User;
import com.userservice.exception.UserAlreadyExistsException;
import com.userservice.exception.UserNotFoundException;
import com.userservice.kafka.UserEvent;
import com.userservice.kafka.UserEventProducer;
import com.userservice.mapper.UserMapper;
import com.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UserServiceImpl unit tests")
class UserServiceImplTest {

    private UserRepository userRepository;
    private UserEventProducer userEventProducer;
    private UserMapper userMapper;
    private UserServiceImpl userService;

    private UserCreateDto createDto;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userEventProducer = mock(UserEventProducer.class);
        userMapper = spy(new UserMapper());
        userService = new UserServiceImpl(userRepository, userMapper, userEventProducer);

        createDto = new UserCreateDto("John Doe", "john@example.com", 30);
    }

    @Test
    @DisplayName("createUser should persist a new user and emit event")
    void createUserShouldPersistAndEmitEvent() {
        when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        UserDto result = userService.createUser(createDto);

        assertNotNull(result.getId());
        assertEquals(createDto.getName(), result.getName());
        assertEquals(createDto.getEmail(), result.getEmail());

        verify(userRepository).existsByEmail(createDto.getEmail());
        verify(userRepository).save(any(User.class));

        ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
        verify(userEventProducer).sendUserCreatedEvent(captor.capture());

        UserEvent producedEvent = captor.getValue();
        assertEquals("USER_CREATED", producedEvent.getEventType());
        assertEquals(result.getId(), producedEvent.getUserId());
        assertEquals(result.getEmail(), producedEvent.getEmail());
    }

    @Test
    @DisplayName("createUser should throw when email already exists")
    void createUserShouldFailOnDuplicateEmail() {
        when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createDto));

        verify(userRepository, never()).save(any());
        verify(userEventProducer, never()).sendUserCreatedEvent(any());
    }

    @Test
    @DisplayName("getUserById should return mapped dto")
    void getUserByIdShouldReturnDto() {
        User stored = buildUser(5L, "Stored", "stored@example.com", 40);
        when(userRepository.findById(5L)).thenReturn(Optional.of(stored));

        UserDto result = userService.getUserById(5L);

        assertEquals(stored.getId(), result.getId());
        assertEquals(stored.getName(), result.getName());
        verify(userRepository).findById(5L);
    }

    @Test
    @DisplayName("getUserById should throw when user missing")
    void getUserByIdShouldThrowWhenMissing() {
        when(userRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(50L));
    }

    @Test
    @DisplayName("getAllUsers should map the complete collection")
    void getAllUsersShouldReturnMappedList() {
        User first = buildUser(1L, "First", "first@example.com", 20);
        User second = buildUser(2L, "Second", "second@example.com", 25);
        when(userRepository.findAll()).thenReturn(List.of(first, second));

        List<UserDto> dtos = userService.getAllUsers();

        assertEquals(2, dtos.size());
        assertTrue(dtos.stream().anyMatch(dto -> "First".equals(dto.getName())));
        assertTrue(dtos.stream().anyMatch(dto -> "Second".equals(dto.getName())));
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("updateUser should throw when user id not found")
    void updateUserShouldThrowWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, new UserUpdateDto()));
    }

    @Test
    @DisplayName("updateUser should throw when new email is already taken")
    void updateUserShouldFailWhenEmailTaken() {
        User stored = buildUser(1L, "John", "john@example.com", 30);
        when(userRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("new@example.com");

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(1L, updateDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser should persist changes and return dto")
    void updateUserShouldPersistChanges() {
        User stored = buildUser(1L, "John", "john@example.com", 30);
        when(userRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(stored)).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateDto updateDto = new UserUpdateDto("Jane", "new@example.com", 31);

        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("Jane", stored.getName());
        assertEquals("new@example.com", stored.getEmail());
        assertEquals(31, stored.getAge());
        assertEquals("Jane", result.getName());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).save(stored);
    }

    @Test
    @DisplayName("deleteUser should throw when user not found")
    void deleteUserShouldThrowWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteUser should remove entity and broadcast event")
    void deleteUserShouldRemoveEntityAndSendEvent() {
        User stored = buildUser(2L, "ToDelete", "delete@example.com", 45);
        when(userRepository.findById(2L)).thenReturn(Optional.of(stored));

        userService.deleteUser(2L);

        verify(userRepository).deleteById(2L);
        ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
        verify(userEventProducer).sendUserDeletedEvent(captor.capture());
        UserEvent event = captor.getValue();
        assertEquals("USER_DELETED", event.getEventType());
        assertEquals(stored.getId(), event.getUserId());
        assertEquals(stored.getEmail(), event.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail should map entity to dto")
    void getUserByEmailShouldReturnDto() {
        User stored = buildUser(3L, "ByEmail", "email@example.com", 29);
        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(stored));

        UserDto result = userService.getUserByEmail("email@example.com");

        assertEquals(stored.getId(), result.getId());
        assertEquals(stored.getEmail(), result.getEmail());
        verify(userRepository).findByEmail("email@example.com");
    }

    @Test
    @DisplayName("getUserByEmail should throw when user missing")
    void getUserByEmailShouldThrowWhenMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("missing@example.com"));
    }

    @Test
    @DisplayName("getUserCount should delegate to repository")
    void getUserCountShouldReturnValue() {
        when(userRepository.count()).thenReturn(42L);

        long count = userService.getUserCount();

        assertEquals(42L, count);
        verify(userRepository).count();
    }

    private User buildUser(Long id, String name, String email, Integer age) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}

