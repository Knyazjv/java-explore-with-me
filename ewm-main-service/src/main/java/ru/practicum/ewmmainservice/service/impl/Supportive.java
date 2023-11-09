package ru.practicum.ewmmainservice.service.impl;

import ru.practicum.ewmmainservice.entity.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewmmainservice.service.impl.ConstString.URI_EVENT;

public class Supportive {

    public static List<String> getUris(List<Event> events) {
        return events.stream().map(event -> URI_EVENT + event.getId()).collect(Collectors.toList());
    }

    public static List<String> getUris(Iterable<Event> events) {
        List<String> result = new ArrayList<>();
        for (Event event : events) {
            result.add(URI_EVENT + event.getId());
        }
        return result;
    }

    public static List<Long> getEventIds(Iterable<Event> events) {
        List<Long> ids = new ArrayList<>();
        for (Event event : events) {
            ids.add(event.getId());
        }
        return ids;
    }
}
