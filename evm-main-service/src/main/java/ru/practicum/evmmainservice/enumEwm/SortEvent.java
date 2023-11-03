package ru.practicum.evmmainservice.enumEwm;

public enum SortEvent {
    EVENT_DATE,
    VIEWS;

    public static SortEvent from(String sort) {
        for (SortEvent value : SortEvent.values()) {
            if (value.name().equals(sort)) {
                return value;
            }
        }
        return null;
    }
}
