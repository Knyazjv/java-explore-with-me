package ru.practicum.ewmmainservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmmainservice.dto.compilation.CompilationDtoRequest;
import ru.practicum.ewmmainservice.dto.compilation.CompilationDtoResponse;
import ru.practicum.ewmmainservice.dto.event.EventDtoShortResponse;
import ru.practicum.ewmmainservice.entity.Compilation;
import ru.practicum.ewmmainservice.entity.Event;
import ru.practicum.ewmmainservice.entity.Request;
import ru.practicum.statsdto.StatsDtoResponse;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MappingCompilation {

    private final MappingEvent mappingEvent;

    public Compilation toCompilation(CompilationDtoRequest cdr, List<Event> events) {
        return new Compilation(null,
                events,
                cdr.getPinned() != null && cdr.getPinned(),
                cdr.getTitle());
    }

    public CompilationDtoResponse toCompilationDtoResponse(List<EventDtoShortResponse> events,
                                                           Compilation compilation) {
        return new CompilationDtoResponse(events,
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle());
    }

    public List<CompilationDtoResponse> toCompilationDtoResponses(List<Compilation> compilations,
                                                                  List<StatsDtoResponse> stats,
                                                                  List<Request> requests) {

        return compilations.stream()
                .map(compilation -> toCompilationDtoResponse(mappingEvent
                        .toEventDtoShortResponses(compilation.getEvents(), stats, requests), compilation))
                .collect(Collectors.toList());

    }

}
