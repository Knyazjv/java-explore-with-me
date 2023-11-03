package ru.practicum.evmmainservice.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.evmmainservice.dto.*;

import java.util.List;

public interface EvmAdminService {
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
