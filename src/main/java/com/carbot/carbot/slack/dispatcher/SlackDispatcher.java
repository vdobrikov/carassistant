package com.carbot.carbot.slack.dispatcher;

import com.carbot.carbot.slack.model.ActionPayload;

import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface SlackDispatcher {
    Optional<HandlerWrapper> getHandlerFor(ActionPayload payload);
    String getDispatchedType();
}
