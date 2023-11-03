package ru.practicum.statsserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsdto.StatsDtoResponse;
import ru.practicum.statsserver.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class StatsController {
    private final StatsService statsService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/hit")
    public ResponseEntity<Void> createHit(@Valid @RequestBody StatsDtoRequest statsDtoRequest) {
        log.info("StatsController Post /hit");
        statsService.createHits(statsDtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(value = "/stats")
    public ResponseEntity<List<StatsDtoResponse>> getStats(@RequestParam String start,
                                                     @RequestParam String end,
                                                     @RequestParam(required = false) List<String> uris,
                                                     @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("StatsController Get /stats");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTimeStart = LocalDateTime.parse(start, formatter);
        LocalDateTime dateTimeEnd = LocalDateTime.parse(end, formatter);

        List<StatsDtoResponse> stats = statsService.getStats(dateTimeStart, dateTimeEnd,
                uris, unique);
        try {
            String s = objectMapper.writeValueAsString(stats);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ResponseEntity<List<StatsDtoResponse>> responseEntity = ResponseEntity.status(HttpStatus.OK).body(stats);


        return responseEntity;
    }

}
