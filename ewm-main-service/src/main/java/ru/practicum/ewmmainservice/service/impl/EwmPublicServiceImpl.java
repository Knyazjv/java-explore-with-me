package ru.practicum.ewmmainservice.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmmainservice.dto.category.CategoryDto;
import ru.practicum.ewmmainservice.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoShortResponse;
import ru.practicum.ewmmainservice.dto.event.GetEventRequest;
import ru.practicum.ewmmainservice.entity.Compilation;
import ru.practicum.ewmmainservice.entity.Event;
import ru.practicum.ewmmainservice.entity.QEvent;
import ru.practicum.ewmmainservice.entity.Request;
import ru.practicum.ewmmainservice.enumEwm.SortEvent;
import ru.practicum.ewmmainservice.enumEwm.State;
import ru.practicum.ewmmainservice.exception.exception.BadRequestException;
import ru.practicum.ewmmainservice.exception.exception.NotFoundException;
import ru.practicum.ewmmainservice.mapper.MappingCategory;
import ru.practicum.ewmmainservice.mapper.MappingCompilation;
import ru.practicum.ewmmainservice.mapper.MappingEvent;
import ru.practicum.ewmmainservice.repository.EwmCategoryRepository;
import ru.practicum.ewmmainservice.repository.EwmCompilationRepository;
import ru.practicum.ewmmainservice.repository.EwmEventRepository;
import ru.practicum.ewmmainservice.repository.EwmRequestRepository;
import ru.practicum.ewmmainservice.service.EwmPublicService;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsdto.StatsDtoResponse;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.practicum.ewmmainservice.service.impl.ConstString.*;
import static ru.practicum.ewmmainservice.service.impl.Supportive.getEventIds;
import static ru.practicum.ewmmainservice.service.impl.Supportive.getUris;

@Service
@RequiredArgsConstructor
public class EwmPublicServiceImpl implements EwmPublicService {

    private final EwmCategoryRepository categoryRepository;
    private final EwmEventRepository eventRepository;
    private final EwmCompilationRepository compilationRepository;
    private final EwmRequestRepository requestRepository;
    private final MappingEvent mappingEvent;
    private final MappingCategory mappingCategory;
    private final MappingCompilation mappingCompilation;
    private final StatsClient statsClient;

    @Override
    public List<CategoryDto> getCategories(PageRequest page) {
        return mappingCategory.toCategoryDtos(categoryRepository.findAll(page).toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        return mappingCategory.toDto(categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY, catId))));
    }

    @Transactional
    @Override
    public List<EventDtoShortResponse> getEvents(GetEventRequest req, Integer from, Integer size,
                                                 HttpServletRequest request) {
        if (req.getRangeStart() != null && req.getRangeStart().isAfter(req.getRangeEnd())) {
            throw new BadRequestException("RangeEnd is after RangeEnd");
        }
        BooleanExpression finalCondition = getCondition(req);
        Sort sort = req.getSort() == null ? Sort.by("id").ascending()
                : getSort(req.getSort());
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        Iterable<Event> events = eventRepository.findAll(finalCondition, pageRequest);
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(events), false).getBody());
        saveStat(request);
        List<Request> requests = requestRepository.findAllByEventIdsAndStatusConfirmed(getEventIds(events));
        return mappingEvent.toEventDtoShortResponses(events, stats, requests);
    }

    @Transactional
    @Override
    public EventDtoResponse getEvent(Long eventId, HttpServletRequest requestHttp) {
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_EVENT, eventId)));
        statsClient.createHit(new StatsDtoRequest("java-explore-with-me",
                URI_EVENT + event.getId(),
                requestHttp.getRemoteAddr(),
                LocalDateTime.now()));
        List<StatsDtoResponse> stats = statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), List.of(URI_EVENT + eventId), true).getBody();
        List<Request> requests = requestRepository.findAllByEventIdAndStatusConfirmed(eventId);
        assert stats != null;
        return mappingEvent.toEventDtoResponse(event,
                stats.isEmpty() ? 0 : stats.get(0).getHits(),
                (long) requests.size());
    }

    @Override
    public List<CompilationDtoResponse> getCompilations(Boolean pinned, PageRequest pageRequest) {
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageRequest).toList();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
        }
        List<Event> allEvents = new ArrayList<>();
        for (Compilation compilation : compilations) {
            allEvents.addAll(compilation.getEvents());
        }
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(allEvents), false).getBody());
        List<Request> requests = requestRepository
                .findAllByEventIdsAndStatusConfirmed(getEventIds(allEvents));
        return mappingCompilation.toCompilationDtoResponses(compilations, stats, requests);
    }

    @Override
    public CompilationDtoResponse getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_COMPILATION, compId)));
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(compilation.getEvents()), false).getBody());
        List<Request> requests = requestRepository
                .findAllByEventIdsAndStatusConfirmed(getEventIds(compilation.getEvents()));
        return mappingCompilation.toCompilationDtoResponse(mappingEvent
                .toEventDtoShortResponses(compilation.getEvents(), stats, requests), compilation);
    }

    private void saveStat(HttpServletRequest request) {
            statsClient.createHit(new StatsDtoRequest("java-explore-with-me",
                    "/events",
                    request.getRemoteAddr().replace(":", "."),
                    LocalDateTime.now()));
    }

    private Sort getSort(SortEvent sortEvent) {
        switch (sortEvent) {
            case EVENT_DATE: return Sort.by("eventDate").ascending();
            case VIEWS: return Sort.by("resolvedUrl").ascending();
            default: return null;
        }
    }

    private BooleanExpression getCondition(GetEventRequest req) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(event.state.eq(State.PUBLISHED));
        if (req.getText() != null && !req.getText().isBlank()) {
            conditions.add(event.annotation.containsIgnoreCase(req.getText())
                    .or(event.description.containsIgnoreCase(req.getText())));
        }
        if (req.getCategories() != null && !req.getCategories().isEmpty()) {
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
}
