package ru.practicum.evmmainservice.service.impl;

public class ConstString {
    static final String NOT_FOUND_CATEGORY = "Category with id=%d was not found";
    static final String NOT_FOUND_USER = "User with id=%d was not found";
    static final String NOT_FOUND_EVENT = "Event with id=%d was not found";
    static final String NOT_FOUND_REQUEST = "Request with id=%d was not found";
    static final String NOT_FOUND_COMPILATION = "Compilation with id=%d was not found";
    static final String NOT_BE_PUBLISHED = "Event must not be published";
    static final String TWO_HOURS_FROM_THE_MOMENT = "The date and time at which the event is scheduled " +
            "cannot be earlier than two hours from the current moment";
    static final String LIMIT = "The participant limit has been reached";
    public static final String URI_EVENT = "/events/";
}
