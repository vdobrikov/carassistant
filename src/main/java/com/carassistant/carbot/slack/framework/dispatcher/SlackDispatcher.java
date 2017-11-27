package com.carassistant.carbot.slack.framework.dispatcher;

import com.carassistant.carbot.slack.framework.model.ActionPayload;

import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface SlackDispatcher {
    Optional<HandlerWrapper> getHandlerFor(ActionPayload payload);
    String getDispatchedType();
}
