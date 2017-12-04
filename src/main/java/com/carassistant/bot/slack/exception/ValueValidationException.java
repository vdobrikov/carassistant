package com.carassistant.bot.slack.exception;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class ValueValidationException extends SlackBotException {
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
