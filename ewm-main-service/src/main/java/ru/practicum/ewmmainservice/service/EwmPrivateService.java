package ru.practicum.ewmmainservice.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewmmainservice.dto.rating.RatingDtoResponse;
import ru.practicum.ewmmainservice.dto.visiting.VisitingDtoResponse;
import ru.practicum.ewmmainservice.dto.event.*;
import ru.practicum.ewmmainservice.dto.request.RequestDtoResponse;

import java.util.List;

public interface EwmPrivateService {
    EventDtoResponse createEvent(EventDtoRequest eventDtoRequest, Long userId);

    EventDtoResponse updateEvent(EventUpdateDtoRequest eventDtoRequest, Long userId, Long eventId);

    List<EventDtoShortResponse> getEvents(Long userId, PageRequest page);

    EventDtoResponse getEventById(Long userId, Long eventId);

    RequestDtoResponse createRequest(Long userId, Long eventId);

    RequestDtoResponse cancelRequest(Long userId, Long requestId);

    List<RequestDtoResponse> getRequests(Long userId);

    List<RequestDtoResponse> getRequestsFromTheInitiator(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);

    VisitingDtoResponse createVisiting(Long userId, Long eventId);

    void deleteVisiting(Long userId, Long eventId);

    RatingDtoResponse createRating(Long userId, Long eventId, Boolean like);

    void deleteRating(Long userId, Long eventId);

    List<VisitingDtoResponse> getVisitings(Long userId, PageRequest of);

    List<RatingDtoResponse> getEstimates(Long userId, PageRequest of);
}
