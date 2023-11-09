package ru.practicum.ewmmainservice.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewmmainservice.dto.category.CategoryDto;
import ru.practicum.ewmmainservice.dto.compilation.CompilationDtoRequest;
import ru.practicum.ewmmainservice.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventUpdateDtoRequest;
import ru.practicum.ewmmainservice.dto.event.GetEventRequestAdmin;
import ru.practicum.ewmmainservice.dto.user.UserDto;

import java.util.List;

public interface EwmAdminService {
    UserDto createUser(UserDto userDto);

    void deleteUser(Long userId);

    List<UserDto> getUsersWithPagination(List<Long> ids, PageRequest pageRequest);

    CategoryDto createCategory(CategoryDto categoryDto);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    CompilationDtoResponse createCompilation(CompilationDtoRequest cdr);

    void deleteCompilation(Long compId);

    CompilationDtoResponse updateCompilation(CompilationDtoRequest cdr, Long compId);

    EventDtoResponse updateEventById(Long eventId, EventUpdateDtoRequest eventDtoRequest);

    List<EventDtoResponse> getEvents(GetEventRequestAdmin req, Integer from, Integer size);
}
