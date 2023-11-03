package ru.practicum.evmmainservice.dto;

import ru.practicum.evmmainservice.enumEwm.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventRequestStatusUpdateRequest {

    @NotNull
    private List<Long> requestIds;

    @NotNull
    private RequestStatus status;

}
