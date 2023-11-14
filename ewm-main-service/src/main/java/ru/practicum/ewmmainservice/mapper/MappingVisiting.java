package ru.practicum.ewmmainservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmmainservice.dto.visiting.VisitingDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoShortResponse;
import ru.practicum.ewmmainservice.entity.Event;
import ru.practicum.ewmmainservice.entity.Request;
import ru.practicum.ewmmainservice.entity.User;
import ru.practicum.ewmmainservice.entity.Visiting;
import ru.practicum.statsdto.StatsDtoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MappingVisiting {

    private final MappingEvent mappingEvent;
    private final MappingUser mappingUser;


    public Visiting toVisiting(User visitor, Event event) {
        return new Visiting(0L, event, visitor, LocalDateTime.now());
    }

    public VisitingDtoResponse toDtoResponse(Visiting visiting,
                                             List<StatsDtoResponse> stats,
                                             List<Request> requests) {
        Long views = (stats == null) || (stats.isEmpty()) ? 0L : stats.get(0).getHits();
        return new VisitingDtoResponse(visiting.getId(), mappingEvent.toEventDtoShortResponse(visiting.getEvent(),
                    views, (long) requests.size()),
                mappingUser.toUserShortDto(visiting.getVisitor()),
                visiting.getCreatedOn());
    }

    public List<VisitingDtoResponse> toDtoResponses(List<Visiting> visitings,
                                                    List<EventDtoShortResponse> shortResponses) {
        Map<Long, EventDtoShortResponse> mapEventDtos = shortResponses.stream()
                .collect(Collectors.toMap(EventDtoShortResponse::getId, dtoShortResponse -> dtoShortResponse));
        return visitings.stream()
                .map(visiting -> new VisitingDtoResponse(visiting.getId(), mapEventDtos.get(visiting.getEvent().getId()),
                                mappingUser.toUserShortDto(visiting.getVisitor()), visiting.getCreatedOn()))
                .collect(Collectors.toList());
    }
}
