package ru.practicum.ewmmainservice.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewmmainservice.dto.category.CategoryDto;
import ru.practicum.ewmmainservice.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoShortResponse;
import ru.practicum.ewmmainservice.dto.event.GetEventRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EwmPublicService {
    List<CategoryDto> getCategories(PageRequest page);

    CategoryDto getCategoryById(Long catId);

    List<EventDtoShortResponse> getEvents(GetEventRequest req, Integer from, Integer size, HttpServletRequest request);

    EventDtoResponse getEvent(Long eventId, HttpServletRequest request);

    List<CompilationDtoResponse> getCompilations(Boolean pinned, PageRequest pageRequest);

    CompilationDtoResponse getCompilationById(Long compId);
}
