package com.carassistant.bot.slack.exception;

import com.carassistant.exception.CarAssistantException;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class SlackBotException extends CarAssistantException {
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
}
