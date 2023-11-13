package ru.practicum.statsserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.statsdto.StatsDtoResponse;
import ru.practicum.statsserver.entity.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {
    @Query("select new ru.practicum.statsdto.StatsDtoResponse(h.app, h.uri, count(h.ip)) " +
            "from Hit h " +
            "where h.timestamp BETWEEN :start AND :end " +
            "and (COALESCE(:uris, '') = '' OR h.uri IN :uris) " +
            "group by h.app, h.uri " +
            "order by count(h.ip) desc ")
    List<StatsDtoResponse> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.statsdto.StatsDtoResponse(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit as h " +
            "where h.timestamp BETWEEN :start AND :end " +
            "and (COALESCE(:uris, '') = '' OR h.uri IN :uris) " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc ")
    List<StatsDtoResponse> getStatsUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);
}

