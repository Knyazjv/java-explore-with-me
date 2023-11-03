package ru.practicum.evmmainservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.evmmainservice.dto.CompilationDtoRequest;
import ru.practicum.evmmainservice.dto.CompilationDtoResponse;
import ru.practicum.evmmainservice.dto.EventDtoShortResponse;
import ru.practicum.evmmainservice.entity.Compilation;
import ru.practicum.evmmainservice.entity.Event;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MappingCompilation {

    public Compilation toCompilation(CompilationDtoRequest cdr, List<Event> events) {
        return new Compilation(null, events, cdr.getPinned(), cdr.getTitle());
    }

    public CompilationDtoResponse toCompilationDtoResponse(List<EventDtoShortResponse> events,
                                                           Compilation compilation) {
        return new CompilationDtoResponse(events,
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle());
    }

}
