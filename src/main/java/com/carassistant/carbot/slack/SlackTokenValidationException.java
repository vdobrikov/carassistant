package com.carassistant.carbot.slack;

import com.carassistant.exception.CarAssistantException;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class SlackTokenValidationException extends CarAssistantException {
    public SlackTokenValidationException() {
    }

    public SlackTokenValidationException(String message) {
        super(message);
    }

    public SlackTokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SlackTokenValidationException(Throwable cause) {
        super(cause);
    }
}
