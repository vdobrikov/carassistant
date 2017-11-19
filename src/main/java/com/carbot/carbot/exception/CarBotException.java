package com.carbot.carbot.exception;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class CarBotException extends RuntimeException {
    public CarBotException() {
    }

    public CarBotException(String message) {
        super(message);
    }

    public CarBotException(String message, Throwable cause) {
        super(message, cause);
    }

    public CarBotException(Throwable cause) {
        super(cause);
    }
}
