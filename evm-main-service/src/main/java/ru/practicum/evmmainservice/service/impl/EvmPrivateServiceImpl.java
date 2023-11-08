package ru.practicum.evmmainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmmainservice.dto.event.*;
import ru.practicum.evmmainservice.dto.request.RequestDtoResponse;
import ru.practicum.evmmainservice.entity.*;
import ru.practicum.evmmainservice.enumEwm.RequestStatus;
import ru.practicum.evmmainservice.enumEwm.State;
import ru.practicum.evmmainservice.enumEwm.StateAction;
import ru.practicum.evmmainservice.exception.exception.BadRequestException;
import ru.practicum.evmmainservice.exception.exception.ConstraintException;
import ru.practicum.evmmainservice.exception.exception.ForbiddenException;
import ru.practicum.evmmainservice.exception.exception.NotFoundException;
import ru.practicum.evmmainservice.mapper.MappingEvent;
import ru.practicum.evmmainservice.mapper.MappingLocation;
import ru.practicum.evmmainservice.mapper.MappingRequest;
import ru.practicum.evmmainservice.repository.*;
import ru.practicum.evmmainservice.service.EvmPrivateService;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.evmmainservice.service.impl.ConstString.*;
import static ru.practicum.evmmainservice.service.impl.Supportive.getEventIds;
import static ru.practicum.evmmainservice.service.impl.Supportive.getUris;

@Service
@RequiredArgsConstructor
public class EvmPrivateServiceImpl implements EvmPrivateService {
    private final EvmEventRepository eventRepository;
    private final EvmLocationRepository locationRepository;
    private final EvmCategoryRepository categoryRepository;
    private final EvmUserRepository userRepository;
    private final EvmRequestRepository requestRepository;
    private final MappingEvent mappingEvent;
    private final MappingLocation mappingLocation;
    private final MappingRequest mappingRequest;
    private final StatsClient statsClient;

    @Transactional
    @Override
    public EventDtoResponse createEvent(EventDtoRequest eventDtoRequest, Long userId) {
        Location location = locationRepository.save(mappingLocation.toLocation(eventDtoRequest.getLocation()));
        Category category = categoryRepository.findById(eventDtoRequest.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY,
                        eventDtoRequest.getCategory())));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        if (eventDtoRequest.getEventDate() != null && LocalDateTime.now().plusHours(2)
                .isAfter(eventDtoRequest.getEventDate())) {
            throw new ForbiddenException("Field: eventDate. Error: должно содержать дату, " +
                    "которая еще не наступила. Value: " + eventDtoRequest.getEventDate());
        }
        Event event = mappingEvent.toEvent(eventDtoRequest, user, category, location);
        return mappingEvent.toEventDtoResponse(eventRepository.save(event), 0L);
    }

    @Transactional
    @Override
    public EventDtoResponse updateEvent(EventUpdateDtoRequest eventDtoRequest, Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_EVENT, eventId)));
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ForbiddenException(NOT_BE_PUBLISHED);
        }
        if (eventDtoRequest.getEventDate() != null && LocalDateTime.now().plusHours(2)
                .isAfter(eventDtoRequest.getEventDate())) {
            throw new BadRequestException(TWO_HOURS_FROM_THE_MOMENT);
        }
        Category category = null;
        if (eventDtoRequest.getCategory() != null) {
            category = categoryRepository.findById(eventDtoRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY,
                            eventDtoRequest.getCategory())));
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        Event newEvent = getUpdateEvent(event, eventDtoRequest, category);
        return mappingEvent.toEventDtoResponse(eventRepository.save(newEvent), 0L);
    }

    @Override
    public List<EventDtoShortResponse> getEvents(Long userId, PageRequest page) {
        List<Event> events = eventRepository.findAllByInitiatorId(userId, page);
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(events), false).getBody());
        List<Request> requests = requestRepository
                .findAllByEventIdsAndStatusConfirmed(getEventIds(events));
        return mappingEvent.toEventDtoShortResponses(events, stats, requests);
    }

    @Override
    public EventDtoResponse getEventById(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        List<StatsDtoResponse> stats = statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), List.of(URI_EVENT + eventId), false).getBody();
        Long views = stats == null || stats.isEmpty() ? 0L : stats.get(0).getHits();
        List<Request> requests = requestRepository.findAllByEventIdAndStatusConfirmed(eventId);
        return mappingEvent.toEventDtoResponse(event, views, (long) requests.size());
    }

    @Transactional
    @Override
    public RequestDtoResponse createRequest(Long requesterId, Long eventId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, requesterId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        if (requestRepository.findByRequester_IdAndEvent_Id(requesterId, eventId).isPresent()) {
            throw new ConstraintException("You can't add a repeat request");
        }
        if (event.getInitiator().getId().equals(requesterId)) {
            throw new ConstraintException("The event initiator cannot add a request to participate in his event");
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ConstraintException("You cannot participate in an unpublished event");
        }
        LocalDateTime dateTime = LocalDateTime.now();
        List<Request> allByEventIdAndStatus = requestRepository
                .findAllByEventIdAndStatusConfirmed(eventId);
        Integer confirmedRequest = (allByEventIdAndStatus == null) || (allByEventIdAndStatus.isEmpty())
                ? 0 : allByEventIdAndStatus.size();
        if (event.getParticipantLimit() <= confirmedRequest && event.getParticipantLimit() != 0) {
            throw new ConstraintException(LIMIT);
        }
        Request request = requestRepository.save(mappingRequest.toRequest(event, requester, dateTime));
        return mappingRequest.toRequestDtoResponse(request);
    }

    @Transactional
    @Override
    public RequestDtoResponse cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_REQUEST, requestId)));
        request.setStatus(RequestStatus.CANCELED);
        Request newRequest = requestRepository.save(request);
        return mappingRequest.toRequestDtoResponse(newRequest);
    }

    @Override
    public List<RequestDtoResponse> getRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return mappingRequest.toRequestDtoResponses(requests);
    }

    @Override
    public List<RequestDtoResponse> getRequestsFromTheInitiator(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return mappingRequest.toRequestDtoResponses(requests);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        List<Request> requests = requestRepository.findAllById(request.getRequestIds());
        List<Request> requestsPending = requests.stream()
                .filter(req -> req.getStatus().equals(RequestStatus.PENDING)).collect(Collectors.toList());
        if (requestsPending.size() == 0) {
            throw new ForbiddenException("Request must have status PENDING");
        }
        RequestStatus status = RequestStatus.from(request.getStatus());
        if (status == null) {
            throw new BadRequestException("Unknown RequestStatus: " + request.getStatus());
        }
        if (status.equals(RequestStatus.REJECTED)) {
            requests.forEach(req -> req.setStatus(RequestStatus.REJECTED));
        } else {
            if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                return new EventRequestStatusUpdateResult();
            }
            long freeLimit = event.getParticipantLimit() - requestRepository
                    .findAllByEventIdAndStatusConfirmed(eventId).size();
            if (freeLimit <= 0) {
                throw new ConstraintException(LIMIT);
            }
            for (Request value : requests) {
                if (freeLimit > 0) {
                    value.setStatus(RequestStatus.CONFIRMED);
                    freeLimit--;
                } else {
                    value.setStatus(RequestStatus.CANCELED);
                }
            }
        }
        requestRepository.saveAll(requests);
        return mappingRequest.toEventRequestStatusUpdateResult(requests);
    }

    private Event getUpdateEvent(Event event, EventUpdateDtoRequest dto, Category category) {
        StateAction stateAction = StateAction.from(dto.getStateAction());
        if (stateAction == StateAction.CANCEL_REVIEW) {
            event.setState(State.CANCELED);
        } else if (stateAction == StateAction.SEND_TO_REVIEW) {
            event.setState(State.PENDING);
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
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
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        return event;
    }
}
