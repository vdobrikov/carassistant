package com.carbot.carbot.slack;

import com.carbot.carbot.exception.CarBotException;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class SlackTokenValidationException extends CarBotException {
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
