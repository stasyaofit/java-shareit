package ru.practicum.shareit.exception;

public class AlreadyExistsMailException extends RuntimeException {
    public AlreadyExistsMailException(String s) {
        super(s);
    }
}
