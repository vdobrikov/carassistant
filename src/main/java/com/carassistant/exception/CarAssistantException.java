package com.carassistant.exception;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class CarAssistantException extends RuntimeException {
    public CarAssistantException() {
    }

    public CarAssistantException(String message) {
        super(message);
    }

    public CarAssistantException(String message, Throwable cause) {
        super(message, cause);
    }

    public CarAssistantException(Throwable cause) {
        super(cause);
    }
}
