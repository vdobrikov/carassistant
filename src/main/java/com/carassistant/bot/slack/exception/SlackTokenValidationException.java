package com.carassistant.bot.slack.exception;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class SlackTokenValidationException extends SlackBotException {
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
