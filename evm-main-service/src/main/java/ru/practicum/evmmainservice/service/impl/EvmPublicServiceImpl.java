package ru.practicum.evmmainservice.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.evmmainservice.dto.CategoryDto;
import ru.practicum.evmmainservice.dto.EventDtoShortResponse;
import ru.practicum.evmmainservice.dto.GetEventRequest;
import ru.practicum.evmmainservice.entity.Event;
import ru.practicum.evmmainservice.entity.QEvent;
import ru.practicum.evmmainservice.enumEwm.SortEvent;
import ru.practicum.evmmainservice.exception.NotFoundException;
import ru.practicum.evmmainservice.mapper.MappingCategory;
import ru.practicum.evmmainservice.mapper.MappingEvent;
import ru.practicum.evmmainservice.repository.EvmCategoryRepository;
import ru.practicum.evmmainservice.repository.EvmEventRepository;
import ru.practicum.evmmainservice.service.EvmPublicService;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsdto.StatsDtoResponse;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EvmPublicServiceImpl implements EvmPublicService {

    private final EvmCategoryRepository categoryRepository;
    private final EvmEventRepository eventRepository;
    private final MappingEvent mappingEvent;
    private final MappingCategory mappingCategory;
    private final StatsClient statsClient;
    private final String NOT_FOUND_CATEGORY = "Category with id=%d was not found";
    private final String NOT_FOUND_EVENT = "Event with id=%d was not found";
    private final String URI_EVENT = "/event/";

    @Override
    public List<CategoryDto> getCategories(PageRequest page) {
        return mappingCategory.toCategoryDtos(categoryRepository.findAll(page).toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        return mappingCategory.toDto(categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY, catId))));
    }

    @Override
    public List<EventDtoShortResponse> getEvents(GetEventRequest req, Integer from, Integer size,
                                                 HttpServletRequest request) {
        BooleanExpression finalCondition = getCondition(req);
        Sort sort = getSort(req.getSort());
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        Iterable<Event> events = eventRepository.findAll(finalCondition, pageRequest);
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(events), false).getBody());
        saveStats(events, request);
        return mappingEvent.toEventDtoShortResponses(events, stats);
    }

    @Override
    public EventDtoShortResponse getEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_EVENT, eventId)));
        statsClient.createHit(new StatsDtoRequest("java-explore-with-me",
                URI_EVENT + event.getId(),
                request.getRemoteAddr(),
                LocalDateTime.now()));
        List<StatsDtoResponse> stats = statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), List.of(URI_EVENT + eventId), false).getBody();
        assert stats != null;
        return mappingEvent.toEventDtoShortResponse(event, stats.get(0).getHits());
    }

    private void saveStats(Iterable<Event> events, HttpServletRequest request) {
        for (Event event : events) {
            statsClient.createHit(new StatsDtoRequest("java-explore-with-me",
                    URI_EVENT + event.getId(),
                    request.getRemoteAddr(),
                    LocalDateTime.now()));
        }
    }

    private Sort getSort(SortEvent sortEvent) {
        switch (sortEvent) {
            case EVENT_DATE: return Sort.by("eventDate").ascending();
            case VIEWS: return Sort.by("resolvedUrl").ascending();
            default: return Sort.by("id").ascending();
        }
    }

    private BooleanExpression getCondition(GetEventRequest req) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        if (!req.getText().isBlank()) {
            conditions.add(event.annotation.containsIgnoreCase(req.getText())
                    .or(event.description.containsIgnoreCase(req.getText())));
        }
        if (req.getCategories() != null || !req.getCategories().isEmpty()) {
            conditions.add(event.category.id.in(req.getCategories()));
        }
        if (req.getPaid() != null) {
            conditions.add(event.paid.eq(req.getPaid()));
        }
        if (req.getOnlyAvailable()) {
            conditions.add(event.confirmedRequests.lt(event.participantLimit));
        }
        if (req.getRangeStart() != null) {
            conditions.add(event.eventDate.after(req.getRangeStart()));
        }
        if (req.getRangeEnd() != null) {
            conditions.add(event.eventDate.before(req.getRangeEnd()));
        }
        return conditions.stream()
                .reduce(BooleanExpression::and)
                .get();
    }

    private List<String> getUris(Iterable<Event> events) {
        List<String> result = new ArrayList<>();
        for (Event event : events) {
            result.add(URI_EVENT + event.getId());
        }
        return result;
    }
}
