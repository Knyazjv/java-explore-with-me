package ru.practicum.evmmainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmmainservice.dto.*;
import ru.practicum.evmmainservice.service.EvmPrivateService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/users")
@RequiredArgsConstructor
public class EvmPrivateController {

    private final EvmPrivateService evmPrivateService;

    @PostMapping(value = "/{userId}/events")
    public ResponseEntity<EventDtoResponse> createEvent(@PathVariable Long userId,
                                                        @RequestBody EventDtoRequest eventDtoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evmPrivateService.createEvent(eventDtoRequest, userId));
    }

    @PatchMapping(value = "/{userId}/events/{eventId}")
    public ResponseEntity<EventDtoResponse> updateEvent(@PathVariable Long userId,
                                                        @PathVariable Long eventId,
                                                        @Valid @RequestBody EventDtoRequest eventDtoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evmPrivateService.updateEvent(eventDtoRequest, userId, eventId));
    }

    @GetMapping(value = "/{userId}/events")
    public ResponseEntity<List<EventDtoShortResponse>> getEvents(@PathVariable Long userId,
                                                                 @RequestParam(defaultValue = "0") Integer from,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(evmPrivateService.getEvents(userId, PageRequest.of(from / size, size)));
    }

    @GetMapping(value = "/{userId}/events/{eventId}")
    public ResponseEntity<EventDtoResponse> getEventsById(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
       return ResponseEntity.status(HttpStatus.OK).body(evmPrivateService.getEventById(userId, eventId));
    }

    @PostMapping(value = "/{userId}/requests")
    public ResponseEntity<RequestDtoResponse> createRequest(@PathVariable Long userId,
                                                            @RequestParam Long eventId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evmPrivateService.createRequest(userId, eventId));
    }

    @PatchMapping(value = "/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<RequestDtoResponse> canselRequest(@PathVariable Long userId,
                                                            @PathVariable Long requestId) {
        return ResponseEntity.status(HttpStatus.OK).body(evmPrivateService.cancelRequest(userId, requestId));
    }

    @GetMapping(value = "/{userId}/requests")
    public ResponseEntity<List<RequestDtoResponse>> getRequests(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(evmPrivateService.getRequests(userId));
    }

    @GetMapping(value = "/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<RequestDtoResponse>> getRequestsByInitiator(@PathVariable Long userId,
                                                                           @PathVariable Long eventId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(evmPrivateService.getRequestsFromTheInitiator(userId, eventId));
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateStatusRequest(@PathVariable Long userId,
                                                        @PathVariable Long eventId,
                                                        @RequestBody EventRequestStatusUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(evmPrivateService.updateStatusRequest(userId, eventId, request));
    }

}
