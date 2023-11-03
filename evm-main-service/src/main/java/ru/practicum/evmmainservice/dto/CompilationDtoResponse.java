package ru.practicum.evmmainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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