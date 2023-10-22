package ru.practicum.statsserver.service;

import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void createHits(StatsDtoRequest statsDtoRequest);

    List<StatsDtoResponse> getStats(LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd, List<String> uris, Boolean unique);
}
