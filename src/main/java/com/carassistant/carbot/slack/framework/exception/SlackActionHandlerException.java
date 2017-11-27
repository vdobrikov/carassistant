package com.carassistant.carbot.slack.framework.exception;

import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.SlackBotException;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class SlackActionHandlerException extends SlackBotException {
    private ActionPayload payload;

    public SlackActionHandlerException(ActionPayload payload, String message) {
        this(message);
        this.payload = payload;
    }

    public SlackActionHandlerException(ActionPayload payload, String message, Throwable cause) {
        super(message, cause);
        this.payload = payload;
    }

    public SlackActionHandlerException(String message) {
        super(message);
    }

    public ActionPayload getPayload() {
        return payload;
    }

    @Override
    public String getMessage() {
        return String.format("%s: payload=%s", super.getMessage(), payload);
    }
}
