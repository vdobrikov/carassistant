package com.carbot.carbot.slack.controller;

import com.carbot.carbot.exception.CarBotException;
import com.carbot.carbot.slack.SlackTokenValidationException;
import com.carbot.carbot.slack.exception.SlackActionHandlerException;
import com.carbot.carbot.slack.dispatcher.HandlerWrapper;
import com.carbot.carbot.slack.dispatcher.SlackDispatcher;
import com.carbot.carbot.slack.model.ActionPayload;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@RestController
public class SlackActionDispatcherController {
    private static final Logger LOG = LoggerFactory.getLogger(SlackActionDispatcherController.class);

    private List<SlackDispatcher> dispatchers;

    private String verificationToken;
    private Gson gson = new Gson();

    @Autowired
    public SlackActionDispatcherController(
        @Value("${slack.verification.token}") String verificationToken, List<SlackDispatcher> dispatchers) {

        this.dispatchers = dispatchers;
        this.verificationToken = verificationToken;
    }

    @RequestMapping(value = "action",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object onReceiveAction(@RequestParam("payload") String rawPayload) throws IOException, SlackApiException {
        LOG.debug("rawPayload={}", rawPayload);
        ActionPayload payload;

        try {
            payload = gson.fromJson(rawPayload, ActionPayload.class);
        } catch (Exception e) {
            throw new CarBotException("Failed to parse payload: " + rawPayload, e);
        }

        if (!StringUtils.equals(verificationToken, payload.getToken())) {
            LOG.error("Wrong verification token in request: {}", payload.getToken());
            throw new SlackTokenValidationException("Wrong verification token in request: " + payload.getToken());
        }

        String type = payload.getType();
        Assert.hasLength(type, "'type' cannot be null or empty");

        SlackDispatcher actionDispatcher = dispatchers.stream()
            .filter(dispatcher -> type.equalsIgnoreCase(dispatcher.getDispatchedType()))
            .findFirst()
            .orElseThrow(() -> {
                LOG.error("No dispatcher found for type={} payload={}", type, payload);
                return new SlackActionHandlerException(payload, "No dispatcher found for type=" + type);
            });

        HandlerWrapper handler = actionDispatcher.getHandlerFor(payload)
            .orElseThrow(() -> {
                LOG.error("No handler found for type={} callbackId={} actions={} payload={}", type, payload.getCallbackId(), payload.getActions(), payload);
                return new SlackActionHandlerException(payload, "No handler found for payload");
            });

        return invokeHandlerMethod(handler, payload);
    }

    private Object invokeHandlerMethod(HandlerWrapper handler, ActionPayload payload) {
        try {
            return handler.getMethod().invoke(handler.getObject(), payload);
        } catch (Exception e) {
            LOG.error("Failed to invoke handler {}", handler, e);
            throw new SlackActionHandlerException(payload, "Failed to invoke handler: " + handler, e);
        }
    }
}
