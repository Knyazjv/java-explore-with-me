package ru.practicum.evmmainservice.mapper;

import ru.practicum.evmmainservice.enumEwm.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.evmmainservice.dto.EventDtoRequest;
import ru.practicum.evmmainservice.dto.EventDtoResponse;
import ru.practicum.evmmainservice.dto.EventDtoShortResponse;
import ru.practicum.evmmainservice.entity.Category;
import ru.practicum.evmmainservice.entity.Event;
import ru.practicum.evmmainservice.entity.Location;
import ru.practicum.evmmainservice.entity.User;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MappingEvent {
    private final MappingCategory mappingCategory;
    private final MappingUser mappingUser;
    private final MappingLocation mapperLocation;

    public Event toEvent(EventDtoRequest edr, User user, Category category, Location location) {
        return new Event(null,
                edr.getAnnotation(),
                category,
                0L,
                LocalDateTime.now(),
                edr.getDescription(),
                edr.getEventDate(),
                user,
                location,
                edr.getPaid(),
                edr.getParticipantLimit(),
                null,
                edr.getRequestModeration(),
                State.PENDING,
                edr.getTitle());
    }

    public Event toEventWithId(Long id, EventDtoRequest edr, User user, Category category, Location location) {
        return new Event(id,
                edr.getAnnotation(),
                category,
                0L,
                LocalDateTime.now(),
                edr.getDescription(),
                edr.getEventDate(),
                user,
                location,
                edr.getPaid(),
                edr.getParticipantLimit(),
                null,
                edr.getRequestModeration(),
                State.PENDING,
                edr.getTitle());
    }

    public EventDtoResponse toEventDtoResponse(Event event, Long views) {
        return new EventDtoResponse(event.getAnnotation(),
                mappingCategory.toDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                event.getId(),
                mappingUser.toUserShortDto(event.getInitiator()),
                mapperLocation.toDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views);
    }

    public EventDtoShortResponse toEventDtoShortResponse(Event event, Long views) {
        return new EventDtoShortResponse(event.getAnnotation(),
                mappingCategory.toDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                event.getId(),
                mappingUser.toUserShortDto(event.getInitiator()),
                event.getPaid(),
                event.getTitle(),
                views);
    }

    public List<EventDtoShortResponse> toEventDtoShortResponses(List<Event> events, List<StatsDtoResponse> stats) {
        Map<Long, Long> map = stats.stream()
                .collect(Collectors.toMap((statsDtoResponse) -> {
                    return Long.parseLong(statsDtoResponse.getUri().replace("/event/", ""));
                }, StatsDtoResponse::getHits, (val1, val2) -> val1));
        return events.stream()
                .map(event -> toEventDtoShortResponse(event, map.get(event.getId())))
                .collect(Collectors.toList());
    }

    public List<EventDtoShortResponse> toEventDtoShortResponses(Iterable<Event> events, List<StatsDtoResponse> stats) {
        Map<Long, Long> map = stats.stream()
                .collect(Collectors.toMap((statsDtoResponse) -> {
                    return Long.parseLong(statsDtoResponse.getUri().replace("/event/", ""));
                }, StatsDtoResponse::getHits, (val1, val2) -> val1));
        List<EventDtoShortResponse> dtos = new ArrayList<>();
        for (Event event : events) {
            dtos.add(toEventDtoShortResponse(event, map.get(event.getId())));
        }
        return dtos;
    }

    public List<EventDtoResponse> toEventDtoResponses(Iterable<Event> events, List<StatsDtoResponse> stats) {
        Map<Long, Long> map = stats.stream()
                .collect(Collectors.toMap((statsDtoResponse) -> {
                    return Long.parseLong(statsDtoResponse.getUri().replace("/event/", ""));
                }, StatsDtoResponse::getHits, (val1, val2) -> val1));
        List<EventDtoResponse> dtos = new ArrayList<>();
        for (Event event : events) {
            dtos.add(toEventDtoResponse(event, map.get(event.getId())));
        }
        return dtos;
    }
}
