package com.carassistant.exception;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideNotFoundException extends EntityNotFoundException {

    public RideNotFoundException() {
    }

    public RideNotFoundException(String message) {
        super(message);
    }

    public RideNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RideNotFoundException(Throwable cause) {
        super(cause);
    }
}
