package ru.practicum.statsdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatsDtoResponse {
    private String app;

    private String uri;

    private Long hits;
}
