package ru.practicum.ewmmainservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewmmainservice.dto.user.UserShortDto;
import ru.practicum.ewmmainservice.dto.category.CategoryDto;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventDtoShortResponse {
    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    private String createdOn;

    private String description;

    private String eventDate;

    private Long id;

    private UserShortDto initiator;
    
    private Boolean paid;

    private String title;

    private Long views;
}