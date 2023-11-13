package ru.practicum.ewmmainservice.enumEwm;

public enum StateAction {
    CANCEL_REVIEW,
    PUBLISH_EVENT,
    REJECT_EVENT,
    SEND_TO_REVIEW;

    public static StateAction from(String stateAction) {
        for (StateAction value : StateAction.values()) {
            if (value.name().equals(stateAction)) {
                return value;
            }
        }
        return null;
    }
}
