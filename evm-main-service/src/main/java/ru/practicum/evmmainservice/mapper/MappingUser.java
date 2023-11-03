package ru.practicum.evmmainservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.evmmainservice.dto.UserDto;
import ru.practicum.evmmainservice.dto.UserShortDto;
import ru.practicum.evmmainservice.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MappingUser {
    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public User toUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }

    public List<UserDto> toDtos(List<User> users) {
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    public UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}
