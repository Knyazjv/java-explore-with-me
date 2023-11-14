package ru.practicum.ewmmainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmmainservice.dto.rating.RatingDtoResponse;
import ru.practicum.ewmmainservice.dto.visiting.VisitingDtoResponse;
import ru.practicum.ewmmainservice.dto.event.*;
import ru.practicum.ewmmainservice.dto.request.RequestDtoResponse;
import ru.practicum.ewmmainservice.entity.*;
import ru.practicum.ewmmainservice.enumEwm.RequestStatus;
import ru.practicum.ewmmainservice.enumEwm.State;
import ru.practicum.ewmmainservice.enumEwm.StateAction;
import ru.practicum.ewmmainservice.exception.exception.*;
import ru.practicum.ewmmainservice.mapper.*;
import ru.practicum.ewmmainservice.repository.*;
import ru.practicum.ewmmainservice.service.EwmPrivateService;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewmmainservice.service.impl.ConstString.*;
import static ru.practicum.ewmmainservice.service.impl.Supportive.*;

@Service
@RequiredArgsConstructor
public class EwmPrivateServiceImpl implements EwmPrivateService {
    private final EwmEventRepository eventRepository;
    private final EwmLocationRepository locationRepository;
    private final EwmCategoryRepository categoryRepository;
    private final EwmUserRepository userRepository;
    private final EwmRequestRepository requestRepository;
    private final EwmRatingRepository ratingRepository;
    private final EwmVisitingRepository visitingRepository;
    private final MappingVisiting mappingVisiting;
    private final MappingRating mappingRating;
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
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(getDateStart(events),
                LocalDateTime.now(), getUris(events), false).getBody());
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
        Long views = 0L;
        if (event.getPublishedOn() != null) {
            List<StatsDtoResponse> stats = statsClient.getStats(event.getPublishedOn(),
                    LocalDateTime.now(), List.of(URI_EVENT + eventId), false).getBody();
            if (stats != null && !stats.isEmpty()) {
                views = stats.get(0).getHits();
            }
        }
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

    @Transactional
    @Override
    public VisitingDtoResponse createVisiting(Long userId, Long eventId) {
        User visitor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        Optional<Visiting> visitingOptional = visitingRepository.findByEventIdAndVisitorId(eventId, userId);
        if (visitingOptional.isPresent()) {
            throw new ConflictException("You have already added a visit");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ConflictException("You cannot attend an upcoming event");
        }
        if (event.getState() != State.PUBLISHED || event.getPublishedOn() == null) {
            throw new ConstraintException("You cannot attend an unpublished event.");
        }
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(event.getPublishedOn(),
                LocalDateTime.now(), List.of(URI_EVENT + eventId), false).getBody());
        List<Request> requests = requestRepository.findAllByEventIdAndStatusConfirmed(eventId);
        Visiting visiting = mappingVisiting.toVisiting(visitor, event);
        return mappingVisiting.toDtoResponse(visitingRepository.save(visiting), stats, requests);
    }

    @Transactional
    @Override
    public void deleteVisiting(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        Visiting visiting = visitingRepository.findByEventIdAndVisitorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_VISITING, userId, eventId)));
        Optional<Rating> rating = ratingRepository.findByEventIdAndEstimatorId(eventId, userId);
        rating.ifPresent(ratingRepository::delete);
        visitingRepository.delete(visiting);
    }

    @Transactional
    @Override
    public RatingDtoResponse createRating(Long userId, Long eventId, Boolean like) {
        User estimator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        visitingRepository.findByEventIdAndVisitorId(eventId, userId)
                .orElseThrow(() -> new ConflictException(String.format(NOT_FOUND_VISITING, userId, eventId)));
        Optional<Rating> ratingOptional = ratingRepository.findByEventIdAndEstimatorId(eventId, userId);
        Rating rating;
        if (ratingOptional.isEmpty()) {
            rating = ratingRepository.save(mappingRating.toRating(event, estimator, like));
        } else {
            rating = ratingOptional.get();
            rating.setLike(like);
            rating = ratingRepository.save(rating);
        }
        event = updateRating(event);
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(event.getPublishedOn(),
                LocalDateTime.now(), List.of(URI_EVENT + eventId), false).getBody());
        List<Request> requests = requestRepository.findAllByEventIdAndStatusConfirmed(eventId);
        return mappingRating.toDtoResponse(rating, event, estimator, stats, requests);
    }

    @Transactional
    @Override
    public void deleteRating(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT + eventId));
        Rating rating = ratingRepository.findByEventIdAndEstimatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_COMPILATION));
        ratingRepository.deleteById(rating.getId());
        updateRating(event);
    }

    @Override
    public List<VisitingDtoResponse> getVisitings(Long userId, PageRequest page) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        List<Visiting> visitings = visitingRepository.findAllByVisitorId(userId, page);
        List<Event> events = visitings.stream().
                map(Visiting::getEvent)
                .collect(Collectors.toList());
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(getDateStart(events),
                LocalDateTime.now(), getUris(events), false).getBody());
        List<Request> requests = requestRepository
                .findAllByEventIdsAndStatusConfirmed(getEventIds(events));
        return mappingVisiting.toDtoResponses(visitings,
                mappingEvent.toEventDtoShortResponses(events, stats, requests));
    }

    @Override
    public List<RatingDtoResponse> getEstimates(Long userId, PageRequest page) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userId)));
        List<Rating> ratings = ratingRepository.findAllByEstimatorId(userId, page);
        List<Event> events = ratings.stream().
                map(Rating::getEvent)
                .collect(Collectors.toList());
        List<StatsDtoResponse> stats = Objects.requireNonNull(statsClient.getStats(getDateStart(events),
                        LocalDateTime.now(), getUris(events), false).getBody());
        List<Request> requests = requestRepository
                .findAllByEventIdsAndStatusConfirmed(getEventIds(events));
        return mappingRating.toDtoResponses(ratings,
                mappingEvent.toEventDtoShortResponses(events, stats, requests));
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

    private Event updateRating(Event event) {
        List<Rating> ratings = ratingRepository.findAllByEventId(event.getId());
        event.setRating(getRatingEvent(ratings));
        User initiator = userRepository.findById(event.getInitiator().getId())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, event.getInitiator().getId())));
        List<Rating> ratingsInitiator = ratingRepository.findAllByEstimatorId(event.getInitiator().getId());
        initiator.setRating(getRatingUser(ratingsInitiator));
        event.setInitiator(initiator);
        userRepository.save(initiator);
        return eventRepository.save(event);
    }

    private Double getRatingUser(List<Rating> ratings) {
        double count = 0d;
        for (Rating rating : ratings) {
            count += rating.getEvent().getRating();
        }
        if (ratings.isEmpty()) {
            return count;
        }
        return count / ratings.size();
    }

    private Double getRatingEvent(List<Rating> ratings) {
        double sizeLike = 0d;
        double sizeDislike = 0d;
        for (Rating rating : ratings) {
            if (rating.getLike()) {
                sizeLike++;
            } else {
                sizeDislike++;
            }
        }
        if (sizeLike == 0d && sizeDislike == 0d) {
            return 0d;
        }
        return sizeLike/(sizeLike + sizeDislike) * 10d;
    }
}
