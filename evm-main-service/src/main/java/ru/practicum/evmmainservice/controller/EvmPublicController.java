package ru.practicum.evmmainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmmainservice.dto.CategoryDto;
import ru.practicum.evmmainservice.dto.EventDtoShortResponse;
import ru.practicum.evmmainservice.dto.GetEventRequest;
import ru.practicum.evmmainservice.service.EvmPublicService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EvmPublicController {

    private final EvmPublicService evmPublicService;


    @GetMapping(value = "/categories")
    public ResponseEntity<List<CategoryDto>> getCategories(@RequestParam(defaultValue = "0") Integer from,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(evmPublicService.getCategories(PageRequest.of(from / size, size)));
    }

    @GetMapping(value = "/categories/{catId}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long catId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(evmPublicService.getCategoryById(catId));
    }

    @GetMapping(value = "/events")
    public ResponseEntity<List<EventDtoShortResponse>> getEventsPublic(@RequestParam(required = false) String text,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) Boolean paid,
                                        @RequestParam(required = false) String rangeStart,
                                        @RequestParam(required = false) String rangeEnd,
                                        @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                        @RequestParam(required = false) String sort,
                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(evmPublicService.getEvents(GetEventRequest.of(text,
                        categories,
                        paid,
                        rangeStart,
                        rangeEnd,
                        onlyAvailable,
                        sort),
                        from,
                        size,
                        request));
    }

    @GetMapping(value = "/events/{eventId}")
    public ResponseEntity<EventDtoShortResponse> getEvent(@PathVariable Long eventId,
                                                          HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(evmPublicService.getEvent(eventId, request));
    }

}
