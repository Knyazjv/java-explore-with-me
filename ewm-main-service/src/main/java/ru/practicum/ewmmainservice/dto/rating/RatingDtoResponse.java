package ru.practicum.ewmmainservice.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmmainservice.dto.event.EventDtoShortResponse;
import ru.practicum.ewmmainservice.dto.user.UserShortDto;

@Setter
@Getter
@AllArgsConstructor
public class RatingDtoResponse {

    private Long id;

    private EventDtoShortResponse event;

    private UserShortDto user;

    private Boolean mark;
}
