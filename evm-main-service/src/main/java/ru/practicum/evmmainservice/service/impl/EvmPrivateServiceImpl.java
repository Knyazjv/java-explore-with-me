package ru.practicum.evmmainservice.service.impl;

import ru.practicum.evmmainservice.enumEwm.RequestStatus;
import ru.practicum.evmmainservice.enumEwm.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.evmmainservice.dto.*;
import ru.practicum.evmmainservice.entity.*;
import ru.practicum.evmmainservice.exception.*;
import ru.practicum.evmmainservice.mapper.*;
import ru.practicum.evmmainservice.repository.*;
import ru.practicum.evmmainservice.service.EvmPrivateService;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final String NOT_FOUND_CATEGORY = "Category with id=%d was not found";
    private final String NOT_FOUND_USER = "User with id=%d was not found";
    private final String NOT_FOUND_EVENT = "Event with id=%d was not found";
    private final String NOT_FOUND_LOCATION = "Location with id=%d was not found";
    private final String NOT_FOUND_REQUEST = "Request with id=%d was not found";
    private final String NOT_BE_PUBLISHED = "Event must not be published";
    private final String TWO_HOURS_FROM_THE_MOMENT = "The date and time at which the event is scheduled " +
            "cannot be earlier than two hours from the current moment";
    private final String LIMIT = "The participant limit has been reached";
    private final String URI_EVENT = "/event/";


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

    @Override
    public EventDtoResponse updateEvent(EventDtoRequest eventDtoRequest, Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_EVENT, eventId)));
        if (event.getState().equals(State.PUBLISHED)) {
            throw new BadRequestException(NOT_BE_PUBLISHED);
        }
        if (eventDtoRequest.getEventDate() != null && LocalDateTime.now().plusHours(2)
                .isAfter(eventDtoRequest.getEventDate())) {
            throw new ForbiddenException(TWO_HOURS_FROM_THE_MOMENT);
        }
        Category category = categoryRepository.findById(eventDtoRequest.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_CATEGORY,
                        eventDtoRequest.getCategory())));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        Location location = locationRepository.save(mappingLocation.
                toLocationWithId(event.getLocation().getId(), eventDtoRequest.getLocation()));
        Event newEvent = mappingEvent.toEventWithId(eventId, eventDtoRequest, user, category, location);
        return mappingEvent.toEventDtoResponse(eventRepository.save(newEvent), 0L);
    }

    @Override
    public List<EventDtoShortResponse> getEvents(Long userId, PageRequest page) {
        List<Event> events = eventRepository.findAllByInitiatorId(userId, page);
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), getUris(events), false).getBody());
        return mappingEvent.toEventDtoShortResponses(events, stats);
    }

    @Override
    public EventDtoResponse getEventById(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        List<StatsDtoResponse> stats = statsClient.getStats(LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusYears(10), List.of(URI_EVENT + eventId), false).getBody();
        assert stats != null;
        return mappingEvent.toEventDtoResponse(event, stats.get(0).getHits());
    }

    @Override
    public RequestDtoResponse createRequest(Long requesterId, Long eventId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, requesterId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        if (requestRepository.findByRequester_IdAndEvent_Id(requesterId, eventId).isEmpty()) {
            throw new ConstraintException("You can't add a repeat request");
        }
        if (event.getInitiator().getId().equals(requesterId)) {
            throw new ConstraintException("The event initiator cannot add a request to participate in his event");
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ConstraintException("You cannot participate in an unpublished event");
        }
        if (event.getParticipantLimit() <= requestRepository
                .findAllByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED).size()) {
            throw new ConstraintException(LIMIT);
        }
        Request request = requestRepository.save(mappingRequest.toRequest(event, requester));
        return mappingRequest.toRequestDtoResponse(request);
    }

    @Override
    public RequestDtoResponse cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_REQUEST, requestId)));
        request.setStatus(RequestStatus.REJECTED);
        Request newRequest = requestRepository.save(request);
        return mappingRequest.toRequestDtoResponse(newRequest);
    }

    @Override
    public List<RequestDtoResponse> getRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        List<Request> requests = requestRepository.findAllByRequester_Id(userId);
        return mappingRequest.toRequestDtoResponses(requests);
    }

    @Override
    public List<RequestDtoResponse> getRequestsFromTheInitiator(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        List<Request> requests = requestRepository.findAllByEvent_Id(eventId);
        return mappingRequest.toRequestDtoResponses(requests);
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        List<Request> requests = requestRepository.findAllById(request.getRequestIds());
        List<Request> requestsNotPending = requests.stream()
                .filter(req -> !req.getStatus().equals(RequestStatus.PENDING)).collect(Collectors.toList());
        if (requestsNotPending.size() == 0) {
            throw new BadRequestException("Request must have status PENDING");
        }
        if (request.getStatus().equals(RequestStatus.REJECTED)) {
            requests.forEach(req -> req.setStatus(RequestStatus.REJECTED));
        } else {
            if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                return new EventRequestStatusUpdateResult();
            }
            long freeLimit = event.getParticipantLimit() - requestRepository
                    .findAllByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED).size();
            if (freeLimit <= 0) {
                throw new ConstraintException(LIMIT);
            }
            for (Request value : requests) {
                if (freeLimit > 0) {
                    value.setStatus(RequestStatus.CONFIRMED);
                    freeLimit--;
                } else {
                    value.setStatus(RequestStatus.REJECTED);
                }
            }
        }
        requestRepository.saveAll(requests);
        return mappingRequest.toEventRequestStatusUpdateResult(requests);
    }

    private List<String> getUris(List<Event> events) {
        return events.stream().map(event -> URI_EVENT + event.getId()).collect(Collectors.toList());
    }
}
