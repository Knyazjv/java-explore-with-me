package ru.practicum.ewmmainservice.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewmmainservice.enumEwm.State;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class GetEventRequestAdmin {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private List<Long> users;
    private List<State> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;

    public static GetEventRequestAdmin of(List<Long> users,
                                          List<String> strStates,
                                          List<Long> categories,
                                          String rangeStart,
                                          String rangeEnd) {

        GetEventRequestAdmin request = new GetEventRequestAdmin();
        request.setUsers(users);
        List<State> states = new ArrayList<>();
        if(strStates != null) {
            State state;
            for (String str : strStates) {
                state = State.from(str);
                if (state != null) {
                    states.add(state);
                }
            }
        }
        request.setStates(states);
        request.setCategories(categories);
        if (request.getRangeStart() != null) {
            request.setRangeStart(LocalDateTime.parse(rangeStart, formatter));
        }
        if (request.getRangeEnd() != null) {
            request.setRangeEnd(LocalDateTime.parse(rangeEnd, formatter));
        }
        return request;
    }
}
