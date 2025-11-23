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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    public final UserEventProducer eventProducer;

    private static final Logger log = LogManager.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.eventProducer = userEventProducer;
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateDto createDto) {
        log.info("Creating user with email: {}", createDto.getEmail());

        if (userRepository.existsByEmail(createDto.getEmail())) {
            log.warn("User with email {} already exists", createDto.getEmail());
            throw new UserAlreadyExistsException(createDto.getEmail());
        }

        User user = userMapper.toEntity(createDto);
        User savedUser = userRepository.save(user);

        UserEvent event = new UserEvent(
                "USER_CREATED",
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getAge()
        );
        eventProducer.sendUserCreatedEvent(event);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");

        List<User> users = userRepository.findAll();

        log.info("Found {} users", users.size());
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (updateDto.getEmail() != null &&
                !updateDto.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(updateDto.getEmail())) {
            log.warn("Email {} is already taken", updateDto.getEmail());
            throw new UserAlreadyExistsException(updateDto.getEmail());
        }

        userMapper.updateEntityFromDto(updateDto, user);

        User updatedUser = userRepository.save(user);

        log.info("User updated successfully: {}", updatedUser.getId());
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        UserEvent event = new UserEvent(
                "USER_DELETED",
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAge()
        );

        userRepository.deleteById(id);
        eventProducer.sendUserDeletedEvent(event);
        log.info("User deleted successfully: {}", id);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

        return userMapper.toDto(user);
    }

    @Override
    public long getUserCount() {
        log.info("Counting users");

        long count = userRepository.count();
        log.info("Total users: {}", count);

        return count;
    }
}