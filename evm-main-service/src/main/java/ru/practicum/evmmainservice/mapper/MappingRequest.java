package ru.practicum.evmmainservice.mapper;

import ru.practicum.evmmainservice.enumEwm.RequestStatus;
import org.springframework.stereotype.Component;
import ru.practicum.evmmainservice.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.evmmainservice.dto.request.RequestDtoResponse;
import ru.practicum.evmmainservice.entity.Event;
import ru.practicum.evmmainservice.entity.Request;
import ru.practicum.evmmainservice.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MappingRequest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public Request toRequest(Event event, User requester, LocalDateTime dateTime) {
        RequestStatus status = event.getRequestModeration() && event.getParticipantLimit() != 0
                ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        return new Request(null, event, requester, status, dateTime);
    }

    public RequestDtoResponse toRequestDtoResponse(Request request) {
        return new RequestDtoResponse(request.getCreated().format(formatter),
                request.getEvent().getId(),
                request.getId(),
                request.getRequester().getId(),
                request.getStatus());
    }

    public List<RequestDtoResponse> toRequestDtoResponses(List<Request> requests) {
        return requests.stream()
                .map(this::toRequestDtoResponse)
                .collect(Collectors.toList());
    }

    public EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(List<Request> requests) {
        List<RequestDtoResponse> confirmedRequests = new ArrayList<>();
        List<RequestDtoResponse> rejectedRequests = new ArrayList<>();
        for (Request request : requests) {
            if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                confirmedRequests.add(toRequestDtoResponse(request));
            } else if (request.getStatus().equals(RequestStatus.REJECTED)) {
                rejectedRequests.add(toRequestDtoResponse(request));
            }
        }
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

}
