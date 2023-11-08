package ru.practicum.evmmainservice.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmmainservice.dto.category.CategoryDto;
import ru.practicum.evmmainservice.dto.compilation.CompilationDtoRequest;
import ru.practicum.evmmainservice.dto.compilation.CompilationDtoResponse;
import ru.practicum.evmmainservice.dto.event.EventDtoResponse;
import ru.practicum.evmmainservice.dto.event.EventUpdateDtoRequest;
import ru.practicum.evmmainservice.dto.event.GetEventRequestAdmin;
import ru.practicum.evmmainservice.dto.user.UserDto;
import ru.practicum.evmmainservice.entity.*;
import ru.practicum.evmmainservice.enumEwm.State;
import ru.practicum.evmmainservice.enumEwm.StateAction;
import ru.practicum.evmmainservice.exception.exception.BadRequestException;
import ru.practicum.evmmainservice.exception.exception.ConflictException;
import ru.practicum.evmmainservice.exception.exception.ForbiddenException;
import ru.practicum.evmmainservice.exception.exception.NotFoundException;
import ru.practicum.evmmainservice.mapper.MappingCategory;
import ru.practicum.evmmainservice.mapper.MappingCompilation;
import ru.practicum.evmmainservice.mapper.MappingEvent;
import ru.practicum.evmmainservice.mapper.MappingUser;
import ru.practicum.evmmainservice.repository.*;
import ru.practicum.evmmainservice.service.EvmAdminService;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.util.*;

import static ru.practicum.evmmainservice.service.impl.ConstString.*;
import static ru.practicum.evmmainservice.service.impl.Supportive.getEventIds;
import static ru.practicum.evmmainservice.service.impl.Supportive.getUris;

@Service
@RequiredArgsConstructor
public class EwmAdminServiceImpl implements EvmAdminService {

    private final EvmUserRepository userRepository;
    private final EvmCategoryRepository categoryRepository;
    private final EvmEventRepository eventRepository;
    private final EvmCompilationRepository compilationRepository;
    private final EvmRequestRepository requestRepository;
    private final StatsClient statsClient;
    private final MappingUser mappingUser;
    private final MappingCategory mappingCategory;
    private final MappingCompilation mappingCompilation;
    private final MappingEvent mappingEvent;

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        return mappingUser.toDto(userRepository.save(mappingUser.toUser(userDto)));
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsersWithPagination(List<Long> ids, PageRequest pageRequest) {
        return ids == null ? mappingUser.toDtos(userRepository.findAll(pageRequest).getContent())
                : mappingUser.toDtos(userRepository.findAllByIds(ids, pageRequest));
    }

    @Transactional
    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        return mappingCategory.toDto(categoryRepository.save(mappingCategory.toCategory(categoryDto)));
    }

    @Transactional
    @Override
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY, catId)));
        List<Event> events = eventRepository.findAllByCategory_Id(catId);
        if(events.size() != 0) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.delete(category);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY, catId)));
        if (categoryDto.getName() != null) {
            category.setName(categoryDto.getName());
        }
        return mappingCategory.toDto(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public CompilationDtoResponse createCompilation(CompilationDtoRequest cdr) {
        List<Event> events;
        if (cdr.getEvents() == null) {
            events = Collections.emptyList();
        } else {
            events = eventRepository.findAllByIdIn(cdr.getEvents());
        }
        Compilation compilation = compilationRepository.save(mappingCompilation.toCompilation(cdr, events));
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(events), false).getBody());
        List<Request> requests = requestRepository
                .findAllByEventIdsAndStatusConfirmed(getEventIds(compilation.getEvents()));
        return mappingCompilation.
                toCompilationDtoResponse(mappingEvent.toEventDtoShortResponses(events, stats, requests), compilation);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_COMPILATION, compId)));
        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDtoResponse updateCompilation(CompilationDtoRequest cdr, Long compId) {
        List<Event> events;
        if (cdr.getEvents() == null) {
            events = new ArrayList<>();
        } else {
            events = eventRepository.findAllByIdIn(cdr.getEvents());
        }
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
        List<Request> requests = requestRepository
                .findAllByEventIdsAndStatusConfirmed(getEventIds(compilation.getEvents()));
        return mappingCompilation.toCompilationDtoResponse(mappingEvent.toEventDtoShortResponses(events,
                stats, requests), newCompilation);
    }

    @Transactional
    @Override
    public EventDtoResponse updateEventById(Long eventId, EventUpdateDtoRequest eventDtoRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_EVENT, eventId)));
        if (LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
            throw new ForbiddenException("The start date of the modified event must " +
                    "be no earlier than an hour from the publication date");
        }
        if (event.getState() == State.PUBLISHED) {
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
        Long views = (stats == null) || (stats.isEmpty()) ? 0 : stats.get(0).getHits();
        List<Request> requests = requestRepository.findAllByEventIdAndStatusConfirmed(eventId);
        return mappingEvent.toEventDtoResponse(eventRepository.save(newEvent), views, (long) requests.size());
    }

    @Override
    public List<EventDtoResponse> getEvents(GetEventRequestAdmin req, Integer from, Integer size) {
        Optional<BooleanExpression> finalCondition = getCondition(req);
        Sort sort = Sort.by("id").ascending();
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        Iterable<Event> events = finalCondition
                .<Iterable<Event>>map(booleanExpression -> eventRepository.findAll(booleanExpression, pageRequest))
                .orElseGet(() -> eventRepository.findAll(pageRequest));
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(events), false).getBody());
        List<Request> requests = requestRepository.findAllByEventIdsAndStatusConfirmed(getEventIds(events));
        return mappingEvent.toEventDtoResponses(events, stats, requests);
    }

    private Optional<BooleanExpression> getCondition(GetEventRequestAdmin req) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        if (req.getUsers() != null && !req.getUsers().isEmpty()) {
            conditions.add(event.initiator.id.in(req.getUsers()));
        }
        if (req.getStates() != null && !req.getStates().isEmpty()) {
            conditions.add(event.state.in(req.getStates()));
        }
        if (req.getCategories() != null && !req.getCategories().isEmpty()) {
            conditions.add(event.category.id.in(req.getCategories()));
        }
        if (req.getRangeStart() != null) {
            conditions.add(event.eventDate.after(req.getRangeStart()));
        }
        if (req.getRangeEnd() != null) {
            conditions.add(event.eventDate.before(req.getRangeEnd()));
        }
        if (conditions.isEmpty()) {
            return Optional.empty();
        } else {
            return conditions.stream().reduce(BooleanExpression::and);
        }
    }

    private Event updateEvent(Event event, EventUpdateDtoRequest dto, Category category) {
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("The date of the event has passed");
            }
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) {
            event.getLocation().setLat(dto.getLocation().getLat());
            event.getLocation().setLon(dto.getLocation().getLon());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
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
        StateAction stateAction = StateAction.from(dto.getStateAction());
        if (stateAction == null && dto.getStateAction() != null) {
            throw new BadRequestException("StateAction unknown:" + dto.getStateAction());
        }
        if (stateAction == StateAction.PUBLISH_EVENT) {
            if (event.getState() == State.CANCELED) {
                throw new ForbiddenException("A canceled event cannot be published");
            }
            event.setState(State.PUBLISHED);
            event.setConfirmedRequests(event.getConfirmedRequests());
        } else if (stateAction == StateAction.REJECT_EVENT) {
            event.setState(State.CANCELED);
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        return event;
    }
}
