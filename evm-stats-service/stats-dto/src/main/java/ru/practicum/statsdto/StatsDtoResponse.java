package ru.practicum.statsdto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatsDtoResponse {
    private String app;

    private String uri;

    private Long hits;

    public StatsDtoResponse(String app, String uri, Long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }
}
