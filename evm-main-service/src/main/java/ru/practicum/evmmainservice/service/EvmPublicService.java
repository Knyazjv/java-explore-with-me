package ru.practicum.evmmainservice.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.evmmainservice.dto.CategoryDto;
import ru.practicum.evmmainservice.dto.EventDtoShortResponse;
import ru.practicum.evmmainservice.dto.GetEventRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EvmPublicService {
    List<CategoryDto> getCategories(PageRequest page);

    CategoryDto getCategoryById(Long catId);

    List<EventDtoShortResponse> getEvents(GetEventRequest req, Integer from, Integer size, HttpServletRequest request);

    EventDtoShortResponse getEvent(Long eventId, HttpServletRequest request);
}
