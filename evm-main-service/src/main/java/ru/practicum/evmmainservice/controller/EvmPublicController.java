package ru.practicum.evmmainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmmainservice.dto.category.CategoryDto;
import ru.practicum.evmmainservice.dto.compilation.CompilationDtoResponse;
import ru.practicum.evmmainservice.dto.event.EventDtoResponse;
import ru.practicum.evmmainservice.dto.event.EventDtoShortResponse;
import ru.practicum.evmmainservice.dto.event.GetEventRequest;
import ru.practicum.evmmainservice.service.EvmPublicService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class EvmPublicController {

    private final EvmPublicService evmPublicService;


    @GetMapping(value = "/categories")
    public ResponseEntity<List<CategoryDto>> getCategories(@RequestParam(defaultValue = "0") Integer from,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /categories");
        return ResponseEntity.status(HttpStatus.OK)
                .body(evmPublicService.getCategories(PageRequest.of(from / size, size)));
    }

    @GetMapping(value = "/categories/{catId}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long catId) {
        log.info("GET /categories/{}", catId);
        return ResponseEntity.status(HttpStatus.OK).body(evmPublicService.getCategoryById(catId));
    }

    @GetMapping(value = "/events")
    public ResponseEntity<List<EventDtoShortResponse>> getEvents(@RequestParam(required = false) String text,
                                                                 @RequestParam(required = false) List<Long> categories,
                                                                 @RequestParam(required = false) Boolean paid,
                                                                 @RequestParam(required = false) String rangeStart,
                                                                 @RequestParam(required = false) String rangeEnd,
                                                                 @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                                                 @RequestParam(required = false) String sort,
                                                                 @RequestParam(defaultValue = "0") Integer from,
                                                                 @RequestParam(defaultValue = "10") Integer size,
                                                                 HttpServletRequest request) {
        log.info("GET /events");
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
    public ResponseEntity<EventDtoResponse> getEvent(@PathVariable Long eventId,
                                                     HttpServletRequest request) {
        log.info("GET /events/{}", eventId);
        return ResponseEntity.status(HttpStatus.OK).body(evmPublicService.getEvent(eventId, request));
    }

    @GetMapping(value = "/compilations")
    public ResponseEntity<List<CompilationDtoResponse>> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                                        @RequestParam(defaultValue = "0") Integer from,
                                                                        @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /compilations");
        return ResponseEntity.status(HttpStatus.OK)
                .body(evmPublicService.getCompilations(pinned, PageRequest.of(from / size, size)));
    }

    @GetMapping(value = "/compilations/{compId}")
    public ResponseEntity<CompilationDtoResponse> getCompilationById(@PathVariable Long compId) {
        log.info("GET /compilations/{}", compId);
        return ResponseEntity.status(HttpStatus.OK).body(evmPublicService.getCompilationById(compId));
    }
}
