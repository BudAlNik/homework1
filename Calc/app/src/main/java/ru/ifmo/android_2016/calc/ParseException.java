package ru.ifmo.android_2016.calc;

public class ParseException extends Exception {
    private final String message;

    public ParseException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
