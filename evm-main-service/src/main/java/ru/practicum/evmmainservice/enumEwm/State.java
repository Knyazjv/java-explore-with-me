package ru.practicum.evmmainservice.enumEwm;

public enum State {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static State from(String state) {
        for (State value : State.values()) {
            if (value.name().equals(state)) {
                return value;
            }
        }
        return null;
    }
}

