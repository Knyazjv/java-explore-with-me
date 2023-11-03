package ru.practicum.evmmainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventRequestStatusUpdateResult {

    private List<RequestDtoResponse> confirmedRequests;

    private List<RequestDtoResponse> rejectedRequests;
}
