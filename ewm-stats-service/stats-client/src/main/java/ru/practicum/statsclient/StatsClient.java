package ru.practicum.statsclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StatsClient {
    private final WebClient webClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${ewm.stats.client.url}") String url) {
        this.webClient = WebClient.create(url);
    }

    public ResponseEntity<Void> createHit(StatsDtoRequest statsDtoRequest) {
        return webClient.post()
                .uri("/hit")
                .body(Mono.just(statsDtoRequest), StatsDtoRequest.class)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<List<StatsDtoResponse>> getStats(LocalDateTime start, LocalDateTime end,
                                                           List<String> uris, Boolean unique) {
        log.info("StatsClient Get /stats?start={}&end={}&uris={}&unique={}", start, end, uris, unique);
        return webClient.get()
                .uri("/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                        start.format(formatter), end.format(formatter),
                        String.join(",", uris), unique)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<StatsDtoResponse>>() {
                })
                .block();
    }
}
