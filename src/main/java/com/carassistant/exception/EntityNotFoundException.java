package com.carassistant.exception;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class EntityNotFoundException extends CarAssistantException {
    public EntityNotFoundException() {
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }
}
