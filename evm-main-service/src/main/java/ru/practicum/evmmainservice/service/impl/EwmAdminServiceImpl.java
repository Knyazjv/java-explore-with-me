package ru.practicum.evmmainservice.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.evmmainservice.dto.*;
import ru.practicum.evmmainservice.entity.Category;
import ru.practicum.evmmainservice.entity.Compilation;
import ru.practicum.evmmainservice.entity.Event;
import ru.practicum.evmmainservice.entity.QEvent;
import ru.practicum.evmmainservice.enumEwm.State;
import ru.practicum.evmmainservice.enumEwm.StateAction;
import ru.practicum.evmmainservice.exception.ConflictException;
import ru.practicum.evmmainservice.exception.ForbiddenException;
import ru.practicum.evmmainservice.exception.NotFoundException;
import ru.practicum.evmmainservice.mapper.*;
import ru.practicum.evmmainservice.repository.*;
import ru.practicum.evmmainservice.service.EvmAdminService;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EwmAdminServiceImpl implements EvmAdminService {

    private final EvmUserRepository userRepository;
    private final EvmCategoryRepository categoryRepository;
    private final EvmEventRepository eventRepository;
    private final EvmCompilationRepository compilationRepository;
    private final StatsClient statsClient;
    private final MappingUser mappingUser;
    private final MappingCategory mappingCategory;
    private final MappingCompilation mappingCompilation;
    private final MappingEvent mappingEvent;
    private final String NOT_FOUND_USER = "User with id=%d was not found";
    private final String NOT_FOUND_CATEGORY = "Category with id=%d was not found";
    private final String URI_EVENT = "/event/";
    private final String NOT_FOUND_COMPILATION = "Compilation with id=%d was not found";
    private final String NOT_FOUND_EVENT = "Event with id=%d was not found";

    @Override
    public UserDto createUser(UserDto userDto) {
        return mappingUser.toDto(userRepository.save(mappingUser.toUser(userDto)));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsersWithPagination(List<Long> ids, PageRequest pageRequest) {
        return mappingUser.toDtos(userRepository.findAllByIds(ids, pageRequest));
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        return mappingCategory.toDto(categoryRepository.save(mappingCategory.toCategory(categoryDto)));
    }

    @Override
    public void deleteCategory(Long catId) {
        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY, catId)));
        List<Event> events = eventRepository.findAllByCategory_Id(catId);
        if(events.size() != 0) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY, catId)));
        return mappingCategory.toDto(categoryRepository.save(mappingCategory.toCategoryWithId(catId, categoryDto)));
    }

    @Override
    public CompilationDtoResponse createCompilation(CompilationDtoRequest cdr) {
        List<Event> events = eventRepository.findAllByIdIn(cdr.getEvents());
        Compilation compilation = compilationRepository.save(mappingCompilation.toCompilation(cdr, events));
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(events), false).getBody());
        return mappingCompilation.toCompilationDtoResponse(mappingEvent.toEventDtoShortResponses(events, stats),
                compilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_COMPILATION, compId)));
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDtoResponse updateCompilation(CompilationDtoRequest cdr, Long compId) {
        List<Event> events = eventRepository.findAllByIdIn(cdr.getEvents());
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_COMPILATION, compId)));
        events.addAll(compilation.getEvents());
        compilation.setEvents(events);
        if (cdr.getPinned() != null) {
            compilation.setPinned(cdr.getPinned());
        }
        if (cdr.getTitle() != null) {
            compilation.setTitle(cdr.getTitle());
        }
        Compilation newCompilation = compilationRepository.save(compilation);
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(events), false).getBody());
        return mappingCompilation.toCompilationDtoResponse(mappingEvent.toEventDtoShortResponses(events, stats),
                newCompilation);
    }

    @Override
    public EventDtoResponse updateEventById(Long eventId, EventUpdateDtoRequest eventDtoRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_EVENT, eventId)));
        if (LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
            throw new ForbiddenException("The start date of the modified event must " +
                    "be no earlier than an hour from the publication date");
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ForbiddenException("Cannot publish the event because " +
                    "it's not in the right state: PUBLISHED");
        }
        Category category;
        if (eventDtoRequest.getCategory() != null) {
            category = categoryRepository.findById(eventDtoRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY,
                            eventDtoRequest.getCategory())));
        } else {
            category = null;
        }
        Event newEvent = updateEvent(event, eventDtoRequest, category);
        List<StatsDtoResponse> stats = statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), List.of(URI_EVENT + eventId), false).getBody();
        assert stats != null;
        return mappingEvent.toEventDtoResponse(eventRepository.save(newEvent),
                stats.get(0).getHits());
    }

    @Override
    public List<EventDtoResponse> getEvents(GetEventRequestAdmin req, Integer from, Integer size) {
        BooleanExpression finalCondition = getCondition(req);
        Sort sort = Sort.by("id").ascending();
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        Iterable<Event> events = eventRepository.findAll(finalCondition, pageRequest);
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(events), false).getBody());
        return mappingEvent.toEventDtoResponses(events, stats);
    }

    private BooleanExpression getCondition(GetEventRequestAdmin req) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        if (req.getUsers() != null || !req.getUsers().isEmpty()) {
            conditions.add(event.initiator.id.in(req.getUsers()));
        }
        if (req.getStates() != null || !req.getUsers().isEmpty()) {
            conditions.add(event.state.in(req.getStates()));
        }
        if (req.getCategories() != null || !req.getCategories().isEmpty()) {
            conditions.add(event.category.id.in(req.getCategories()));
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

    private Event updateEvent(Event event, EventUpdateDtoRequest dto, Category category) {
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) {
            event.getLocation().setLat(dto.getLocation().getLat());
            event.getLocation().setLon(dto.getLocation().getLon());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getStateAction() == StateAction.PUBLISH_EVENT) {
            event.setState(State.PUBLISHED);
        } else if (dto.getStateAction() == StateAction.REJECT_EVENT) {
            event.setState(State.CANCELLED);
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        return event;
    }

    private List<String> getUris(List<Event> events) {
        return events.stream().map(event -> URI_EVENT + event.getId()).collect(Collectors.toList());
    }

    private List<String> getUris(Iterable<Event> events) {
        List<String> result = new ArrayList<>();
        for (Event event : events) {
            result.add(URI_EVENT + event.getId());
        }
        return result;
    }
}
