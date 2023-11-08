package ru.practicum.evmmainservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.evmmainservice.enumEwm.RequestStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RequestDtoResponse {

    private String created;

    private Long event;

    private Long id;

    private Long requester;

    private RequestStatus status;
}

