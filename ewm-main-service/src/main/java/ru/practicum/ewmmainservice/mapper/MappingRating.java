package ru.practicum.ewmmainservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmmainservice.dto.rating.RatingDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoShortResponse;
import ru.practicum.ewmmainservice.entity.*;
import ru.practicum.statsdto.StatsDtoResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MappingRating {

    private final MappingEvent mappingEvent;
    private final MappingUser mappingUser;

    public Rating toRating(Event event, User user, Boolean like) {
        return new Rating(0L, event, user, like);
    }

    public RatingDtoResponse toDtoResponse(Rating rating, Event event, User visitor,
                                           List<StatsDtoResponse> stats,
                                           List<Request> requests) {
        Long views = (stats == null) || (stats.isEmpty()) ? 0L : stats.get(0).getHits();
        return new RatingDtoResponse(rating.getId(),
                mappingEvent.toEventDtoShortResponse(event, views, (long) requests.size()),
                mappingUser.toUserShortDto(visitor), rating.getLike());
    }

    public List<RatingDtoResponse> toDtoResponses(List<Rating> ratings,
                                                    List<EventDtoShortResponse> shortResponses) {
        Map<Long, EventDtoShortResponse> mapEventDtos = shortResponses.stream()
                .collect(Collectors.toMap(EventDtoShortResponse::getId, dtoShortResponse -> dtoShortResponse));
        return ratings.stream()
                .map(rating -> new RatingDtoResponse(rating.getId(), mapEventDtos.get(rating.getEvent().getId()),
                        mappingUser.toUserShortDto(rating.getEstimator()), rating.getLike()))
                .collect(Collectors.toList());
    }
}
