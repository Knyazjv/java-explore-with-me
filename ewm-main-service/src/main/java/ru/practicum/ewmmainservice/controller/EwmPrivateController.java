package ru.practicum.ewmmainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmmainservice.dto.rating.RatingDtoResponse;
import ru.practicum.ewmmainservice.dto.visiting.VisitingDtoResponse;
import ru.practicum.ewmmainservice.dto.event.*;
import ru.practicum.ewmmainservice.dto.request.RequestDtoResponse;
import ru.practicum.ewmmainservice.service.EwmPrivateService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/users")
@RequiredArgsConstructor
public class EwmPrivateController {

    private final EwmPrivateService ewmPrivateService;

    @PostMapping(value = "/{userId}/events")
    public ResponseEntity<EventDtoResponse> createEvent(@PathVariable Long userId,
                                                        @Valid @RequestBody EventDtoRequest eventDtoRequest) {
        log.info("POST /users/{}/events", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ewmPrivateService.createEvent(eventDtoRequest, userId));
    }

    @PatchMapping(value = "/{userId}/events/{eventId}")
    public ResponseEntity<EventDtoResponse> updateEvent(@PathVariable Long userId,
                                                        @PathVariable Long eventId,
                                                        @Valid @RequestBody EventUpdateDtoRequest eventDtoRequest) {
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ewmPrivateService.updateEvent(eventDtoRequest, userId, eventId));
    }

    @GetMapping(value = "/{userId}/events")
    public ResponseEntity<List<EventDtoShortResponse>> getEvents(@PathVariable Long userId,
                                                                 @RequestParam(defaultValue = "0") Integer from,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /users/{}/events", userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ewmPrivateService.getEvents(userId, PageRequest.of(from / size, size)));
    }

    @GetMapping(value = "/{userId}/events/{eventId}")
    public ResponseEntity<EventDtoResponse> getEventsById(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}", userId, eventId);
        return ResponseEntity.status(HttpStatus.OK).body(ewmPrivateService.getEventById(userId, eventId));
    }

    @PostMapping(value = "/{userId}/requests")
    public ResponseEntity<RequestDtoResponse> createRequest(@PathVariable Long userId,
                                                            @RequestParam Long eventId) {
        log.info("POST /users/{}/requests; eventId:{}", userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ewmPrivateService.createRequest(userId, eventId));
    }

    @PatchMapping(value = "/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<RequestDtoResponse> canselRequest(@PathVariable Long userId,
                                                            @PathVariable Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel", userId, requestId);
        return ResponseEntity.status(HttpStatus.OK).body(ewmPrivateService.cancelRequest(userId, requestId));
    }

    @GetMapping(value = "/{userId}/requests")
    public ResponseEntity<List<RequestDtoResponse>> getRequests(@PathVariable Long userId) {
        log.info("GET /users/{}/requests", userId);
        return ResponseEntity.status(HttpStatus.OK).body(ewmPrivateService.getRequests(userId));
    }

    @GetMapping(value = "/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<RequestDtoResponse>> getRequestsByInitiator(@PathVariable Long userId,
                                                                           @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ewmPrivateService.getRequestsFromTheInitiator(userId, eventId));
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateStatusRequest(@PathVariable Long userId,
                                                                              @PathVariable Long eventId,
                                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("PATCH /users/{}/events/{}/requests", userId, eventId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ewmPrivateService.updateStatusRequest(userId, eventId, request));
    }

    @PostMapping(value = "/{userId}/events/{eventId}/visiting")
    public ResponseEntity<VisitingDtoResponse> createVisiting(@PathVariable Long userId,
                                                              @PathVariable Long eventId) {
        log.info("POST /users/{}/events/{}/visiting", userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ewmPrivateService.createVisiting(userId, eventId));
    }

    @DeleteMapping(value = "/{userId}/events/{eventId}/visiting")
    public ResponseEntity<Void> deleteVisiting(@PathVariable Long userId,
                                               @PathVariable Long eventId) {
        log.info("DELETE /users/{}/events/{}/visiting", userId, eventId);
        ewmPrivateService.deleteVisiting(userId, eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(value = "/{userId}/events/{eventId}/like")
    public ResponseEntity<RatingDtoResponse> createRating(@PathVariable Long userId,
                                                          @PathVariable Long eventId,
                                                          @RequestParam Boolean like) {
        log.info("POST /users/{}/events/{}/like; like:{}", userId, eventId, like);
        return ResponseEntity.status(HttpStatus.CREATED).body(ewmPrivateService.createRating(userId, eventId, like));
    }

    @DeleteMapping(value = "/{userId}/events/{eventId}/like")
    public ResponseEntity<Void> deleteRating(@PathVariable Long userId,
                                             @PathVariable Long eventId) {
        log.info("DELETE /users/{}/events/{}/like", userId, eventId);
        ewmPrivateService.deleteRating(userId, eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping(value = "/{userId}/visiting")
    public ResponseEntity<List<VisitingDtoResponse>> getVisitings(@PathVariable Long userId,
                                                           @RequestParam(defaultValue = "0") Integer from,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /users/{}/visiting", userId);
        return ResponseEntity.status(HttpStatus.OK).body(ewmPrivateService.getVisitings(userId,
                PageRequest.of(from / size, size)));
    }

    @GetMapping(value = "/{userId}/like")
    public ResponseEntity<List<RatingDtoResponse>> getUserEstimates(@PathVariable Long userId,
                                                           @RequestParam(defaultValue = "0") Integer from,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /users/{}/like", userId);
        return ResponseEntity.status(HttpStatus.OK).body(ewmPrivateService.getEstimates(userId,
                PageRequest.of(from / size, size)));
    }

}
