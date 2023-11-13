package ru.practicum.ewmmainservice.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmmainservice.dto.event.EventDtoShortResponse;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class CompilationDtoResponse {

    private List<EventDtoShortResponse> events;

    private Long id;

    private Boolean pinned;

    private String title;
}