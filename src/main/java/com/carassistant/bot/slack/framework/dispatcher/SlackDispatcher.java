package com.carassistant.bot.slack.framework.dispatcher;

import com.carassistant.bot.slack.framework.model.ActionPayload;

import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface SlackDispatcher {
    Optional<HandlerExecutionWrapper> getHandlerFor(ActionPayload payload);
    String getDispatchedType();
}
