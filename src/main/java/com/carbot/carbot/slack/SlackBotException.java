package com.carbot.carbot.slack;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class SlackBotException extends RuntimeException {
    public SlackBotException() {
    }

    public SlackBotException(String message) {
        super(message);
    }

    public SlackBotException(String message, Throwable cause) {
        super(message, cause);
    }

    public SlackBotException(Throwable cause) {
        super(cause);
    }

    public SlackBotException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
