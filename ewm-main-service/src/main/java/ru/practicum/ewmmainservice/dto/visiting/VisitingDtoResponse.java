package ru.practicum.ewmmainservice.dto.visiting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmmainservice.dto.event.EventDtoShortResponse;
import ru.practicum.ewmmainservice.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class VisitingDtoResponse {

    private Long id;

    private EventDtoShortResponse event;

    private UserShortDto user;

    private LocalDateTime createdOn;
}
