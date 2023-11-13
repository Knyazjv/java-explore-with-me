package ru.practicum.ewmmainservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewmmainservice.dto.request.RequestDtoResponse;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventRequestStatusUpdateResult {

    private List<RequestDtoResponse> confirmedRequests;

    private List<RequestDtoResponse> rejectedRequests;
}
