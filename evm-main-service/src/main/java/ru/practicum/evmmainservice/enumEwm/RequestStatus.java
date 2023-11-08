package ru.practicum.evmmainservice.enumEwm;

public enum RequestStatus {
    PENDING,
    CONFIRMED,
    CANCELED,
    REJECTED;

    public static RequestStatus from(String status) {
        for (RequestStatus value : RequestStatus.values()) {
            if (value.name().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
