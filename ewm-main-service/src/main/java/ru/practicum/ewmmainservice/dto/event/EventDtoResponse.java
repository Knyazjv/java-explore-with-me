package ru.practicum.ewmmainservice.dto.event;

import ru.practicum.ewmmainservice.dto.location.LocationDto;
import ru.practicum.ewmmainservice.dto.user.UserShortDto;
import ru.practicum.ewmmainservice.dto.category.CategoryDto;
import ru.practicum.ewmmainservice.enumEwm.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventDtoResponse {

    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    private String createdOn;

    private String description;

    private String eventDate;

    private Long id;

    private UserShortDto initiator;
    
    private LocationDto location;
    
    private Boolean paid;
    
    private Long participantLimit;

    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    private State state;

    private String title;

    private Long views;

    private Double rating;
}