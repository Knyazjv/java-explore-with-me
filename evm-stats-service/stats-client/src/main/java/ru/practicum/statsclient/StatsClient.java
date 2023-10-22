package ru.practicum.statsclient;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsdto.StatsDtoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class StatsClient {
    private final WebClient webClient;

    @Autowired
    public StatsClient() {
        this.webClient = WebClient.create("${evm-stats-client.url}");
    }

    public ResponseEntity<Void> createHit(StatsDtoRequest statsDtoRequest) {
        log.info("StatsClient Post /hit");
        return webClient.post()
                .uri("/hit")
                .body(BodyInserters.fromValue(statsDtoRequest))
                .retrieve()
                .toEntity(Void.class)
                .block();
    }

    public ResponseEntity<List<StatsDtoResponse>> getStats(String start, String end,
                                                           List<String> uris, Boolean unique) {
        log.info("StatsClient Get /stats?start={}&end={}&uris={}&unique={}", start, end, uris, unique);
        return webClient.get()
                .uri("/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                        start, end, String.join(",", uris), unique)
                .retrieve()
                .toEntityList(StatsDtoResponse.class)
                .block();
    }
}
