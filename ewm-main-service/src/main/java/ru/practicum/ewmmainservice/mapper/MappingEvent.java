package ru.practicum.ewmmainservice.mapper;

import ru.practicum.ewmmainservice.entity.*;
import ru.practicum.ewmmainservice.enumEwm.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmmainservice.dto.event.EventDtoRequest;
import ru.practicum.ewmmainservice.dto.event.EventDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoShortResponse;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.ewmmainservice.service.impl.ConstString.URI_EVENT;

@RequiredArgsConstructor
@Component
public class MappingEvent {
    private final MappingCategory mappingCategory;
    private final MappingUser mappingUser;
    private final MappingLocation mapperLocation;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
                edr.getPaid() != null && edr.getPaid(),
                edr.getParticipantLimit() == null ? 0 : edr.getParticipantLimit(),
                null,
                edr.getRequestModeration() == null || edr.getRequestModeration(),
                State.PENDING,
                edr.getTitle(),
                0D);
    }

    public EventDtoResponse toEventDtoResponse(Event event, Long views, Long confirmedRequests) {
        if (confirmedRequests == null) {
            confirmedRequests = 0L;
        }
        if (views == null) {
            views = 0L;
        }
        return new EventDtoResponse(event.getAnnotation(),
                mappingCategory.toDto(event.getCategory()),
                confirmedRequests,
                event.getCreatedOn().format(formatter),
                event.getDescription(),
                event.getEventDate().format(formatter),
                event.getId(),
                mappingUser.toUserShortDto(event.getInitiator()),
                mapperLocation.toDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views,
                event.getRating());
    }

    public EventDtoResponse toEventDtoResponse(Event event, Long views) {
        if (views == null) {
            views = 0L;
        }
        return new EventDtoResponse(event.getAnnotation(),
                mappingCategory.toDto(event.getCategory()),
                0L,
                event.getCreatedOn().format(formatter),
                event.getDescription(),
                event.getEventDate().format(formatter),
                event.getId(),
                mappingUser.toUserShortDto(event.getInitiator()),
                mapperLocation.toDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views,
                event.getRating());
    }

    public EventDtoShortResponse toEventDtoShortResponse(Event event, Long views, Long confirmedRequests) {
        if (confirmedRequests == null) {
            confirmedRequests = 0L;
        }
        if (views == null) {
            views = 0L;
        }
        return new EventDtoShortResponse(event.getAnnotation(),
                mappingCategory.toDto(event.getCategory()),
                confirmedRequests,
                event.getCreatedOn().format(formatter),
                event.getDescription(),
                event.getEventDate().format(formatter),
                event.getId(),
                mappingUser.toUserShortDto(event.getInitiator()),
                event.getPaid(),
                event.getTitle(),
                views,
                event.getRating());
    }

    public List<EventDtoShortResponse> toEventDtoShortResponses(List<Event> events, List<StatsDtoResponse> stats,
                                                                List<Request> requests) {
        Map<Long, Long> mapStats = getMapStats(stats);
        Map<Long, Long> mapRequests = getMapRequest(requests);
        return events.stream()
                .map(event -> toEventDtoShortResponse(event, mapStats.get(event.getId()),
                        mapRequests.get(event.getId())))
                .collect(Collectors.toList());
    }



    public List<EventDtoShortResponse> toEventDtoShortResponses(Iterable<Event> events, List<StatsDtoResponse> stats,
                                                                List<Request> requests) {
        Map<Long, Long> mapStats = getMapStats(stats);
        Map<Long, Long> mapRequests = getMapRequest(requests);
        List<EventDtoShortResponse> dtos = new ArrayList<>();
        for (Event event : events) {
            dtos.add(toEventDtoShortResponse(event, mapStats.get(event.getId()), mapRequests.get(event.getId())));
        }
        return dtos;
    }

    public List<EventDtoResponse> toEventDtoResponses(Iterable<Event> events, List<StatsDtoResponse> stats,
                                                      List<Request> requests) {
        Map<Long, Long> mapStats = getMapStats(stats);
        Map<Long, Long> mapRequests = getMapRequest(requests);
        List<EventDtoResponse> dtos = new ArrayList<>();
        for (Event event : events) {
            dtos.add(toEventDtoResponse(event, mapStats.get(event.getId()), mapRequests.get(event.getId())));
        }
        return dtos;
    }

    private Map<Long, Long>  getMapRequest(List<Request> requests) {
        return requests.stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));
    }

    private Map<Long, Long>  getMapStats(List<StatsDtoResponse> stats) {
        return stats.stream()
                .collect(Collectors.toMap((statsDtoResponse) -> {
                    return Long.parseLong(statsDtoResponse.getUri().replace(URI_EVENT, ""));
                }, StatsDtoResponse::getHits, (val1, val2) -> val1));
    }
}
