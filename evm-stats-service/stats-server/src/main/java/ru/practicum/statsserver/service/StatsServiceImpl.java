package ru.practicum.statsserver.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsdto.StatsDtoResponse;
import ru.practicum.statsserver.mapper.MappingHit;
import ru.practicum.statsserver.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final MappingHit mappingHit;

    @Transactional
    @Override
    public void createHits(StatsDtoRequest statsDtoRequest) {
        statsRepository.save(mappingHit.toHit(statsDtoRequest));
    }

    @Override
    public List<StatsDtoResponse> getStats(LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd,
                                           List<String> uris, Boolean unique) {

        return unique ? statsRepository.getStatsUniqueIp(dateTimeStart, dateTimeEnd, uris)
                : statsRepository.getStats(dateTimeStart, dateTimeEnd, uris);
    }
}
