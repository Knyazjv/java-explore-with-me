package ru.practicum.ewmmainservice.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewmmainservice.enumEwm.SortEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class GetEventRequest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private SortEvent sort;

    public static GetEventRequest of(String text,
                                     List<Long> categories,
                                     Boolean paid,
                                     String rangeStart,
                                     String rangeEnd,
                                     Boolean onlyAvailable,
                                     String sort) {
        GetEventRequest request = new GetEventRequest();
        request.setText(text);
        request.setCategories(categories);
        request.setPaid(paid);
        request.setRangeStart(rangeStart == null ? null
                : LocalDateTime.parse(rangeStart, formatter));
        request.setRangeEnd(rangeEnd == null ? null :
                LocalDateTime.parse(rangeEnd, formatter));
        request.setOnlyAvailable(onlyAvailable);
        request.setSort(SortEvent.from(sort));
        return request;
    }

}
