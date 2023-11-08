package ru.practicum.evmmainservice.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.evmmainservice.dto.event.*;
import ru.practicum.evmmainservice.dto.request.RequestDtoResponse;

import java.util.List;

public interface EvmPrivateService {
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
}
