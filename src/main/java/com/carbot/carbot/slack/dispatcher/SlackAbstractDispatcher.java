package com.carbot.carbot.slack.dispatcher;

import com.carbot.carbot.slack.model.ActionPayload;
import org.springframework.util.Assert;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public abstract class SlackAbstractDispatcher implements SlackDispatcher {

    protected void validate(ActionPayload payload) {
        Assert.notNull(payload, "'payload' cannot be null");

        Assert.isTrue(getDispatchedType().equals(payload.getType()), String.format("type should be '%s'", getDispatchedType()));

        String callbackId = payload.getCallbackId();
        Assert.hasLength(callbackId, "callback id cannot be null or empty");
    }
}
