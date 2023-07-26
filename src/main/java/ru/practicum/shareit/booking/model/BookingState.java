package ru.practicum.shareit.booking.model;

import java.util.Optional;

public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static Optional<BookingState> toState(String stringState) {
        for (BookingState bs : values()) {
            if (bs.name().equalsIgnoreCase(stringState)) {
                return Optional.of(bs);
            }
        }
        return Optional.empty();
    }
}