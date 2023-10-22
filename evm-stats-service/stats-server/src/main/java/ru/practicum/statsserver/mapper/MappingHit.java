package ru.practicum.statsserver.mapper;

import ru.practicum.statsdto.StatsDtoRequest;
import ru.practicum.statsserver.entity.Hit;
import org.springframework.stereotype.Component;

@Component
public class MappingHit {
    public Hit toHit(StatsDtoRequest sdt) {
        return new Hit(null, sdt.getApp(), sdt.getUri(), sdt.getIp(), sdt.getTimestamp());
    }
}
