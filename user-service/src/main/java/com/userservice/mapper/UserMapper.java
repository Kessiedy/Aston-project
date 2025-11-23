package com.userservice.mapper;

import com.userservice.dto.UserCreateDto;
import com.userservice.dto.UserDto;
import com.userservice.dto.UserUpdateDto;
import com.userservice.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user){
        if (user == null) return null;
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }

    public User toEntity(UserCreateDto createDto){
        if (createDto == null) return null;
        User user = new User();
        user.setName(createDto.getName());
        user.setEmail(createDto.getEmail());
        user.setAge(createDto.getAge());

        return user;
    }

    public void updateEntityFromDto(UserUpdateDto updateDto, User user){
        if (updateDto == null || user == null) return;

        if (updateDto.getName() != null){
            user.setName(updateDto.getName());
        }

        if (updateDto.getEmail() != null){
            user.setName(updateDto.getName());
        }

        if (updateDto.getAge() != null){
            user.setAge(updateDto.getAge());
        }
    }
}