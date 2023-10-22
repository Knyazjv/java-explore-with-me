package ru.practicum.evmmainservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class EwmControllerTestClient {
    private final StatsClient statsClient;

    public EwmControllerTestClient() {
        this.statsClient = new StatsClient();
    }

    @PostMapping(value = "/test")
    public void save() {
        statsClient.createHit(new StatsDtoRequest("appTest", "uriTest",
                "192.168.123.132", LocalDateTime.now()));
    }

    @GetMapping(value = "/test")
    public ResponseEntity<List<StatsDtoResponse>> getStats() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String start = LocalDateTime.now().minusDays(10).format(formatter);
        String end = LocalDateTime.now().plusDays(10).format(formatter);
        return statsClient.getStats(start, end, List.of("uriTest"), false);
    }
}
