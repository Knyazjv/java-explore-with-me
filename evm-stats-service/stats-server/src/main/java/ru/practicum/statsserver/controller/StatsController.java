package ru.practicum.statsserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsdto.StatsDtoResponse;
import ru.practicum.statsserver.exception.BadRequestException;
import ru.practicum.statsserver.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class StatsController {
    private final StatsService statsService;

    @Transactional
    @PostMapping(value = "/hit")
    public ResponseEntity<Void> createHit(@Valid @RequestBody StatsDtoRequest statsDtoRequest) {
        log.info("StatsController Post /hit");
        statsService.createHits(statsDtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Transactional
    @GetMapping(value = "/stats")
    public ResponseEntity<List<StatsDtoResponse>> getStats(@RequestParam String start,
                                                     @RequestParam String end,
                                                     @RequestParam(required = false) List<String> uris,
                                                     @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("StatsController Get /stats");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTimeStart = LocalDateTime.parse(start, formatter);
        LocalDateTime dateTimeEnd = LocalDateTime.parse(end, formatter);
        if (dateTimeStart.isAfter(dateTimeEnd)) {
            throw new BadRequestException("DateTimeStart is after DateTimeEnd");
        }
        List<StatsDtoResponse> stats;
        if (uris.isEmpty()) {
            stats = Collections.emptyList();
        } else {
            stats = statsService.getStats(dateTimeStart, dateTimeEnd, uris, unique);
        }
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

}
