package ru.practicum.ewmmainservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewmmainservice.dto.user.UserDto;
import ru.practicum.ewmmainservice.dto.user.UserShortDto;
import ru.practicum.ewmmainservice.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MappingUser {
    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRating());
    }

    public User toUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail(), 0D);
    }

    public List<UserDto> toDtos(List<User> users) {
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    public UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}
