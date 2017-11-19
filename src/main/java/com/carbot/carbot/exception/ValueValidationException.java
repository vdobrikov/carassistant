package com.carbot.carbot.exception;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class ValueValidationException extends RuntimeException {
    public ValueValidationException() {
    }

    public ValueValidationException(String message) {
        super(message);
    }

    public ValueValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueValidationException(Throwable cause) {
        super(cause);
    }
}
