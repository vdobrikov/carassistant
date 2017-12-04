package com.carassistant.bot.slack.framework.controller;

import com.carassistant.bot.slack.exception.SlackTokenValidationException;
import com.carassistant.bot.slack.framework.CallbackMatcher;
import com.carassistant.bot.slack.framework.UserContext;
import com.carassistant.bot.slack.framework.annotation.CallbackVariable;
import com.carassistant.bot.slack.framework.dispatcher.HandlerExecutionWrapper;
import com.carassistant.bot.slack.framework.dispatcher.SlackDispatcher;
import com.carassistant.bot.slack.framework.exception.SlackActionHandlerException;
import com.carassistant.bot.slack.framework.model.Action;
import com.carassistant.bot.slack.framework.model.ActionPayload;
import com.carassistant.exception.CarAssistantException;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@RestController
public class SlackActionDispatcherController {
    private static final Logger LOG = LoggerFactory.getLogger(SlackActionDispatcherController.class);
    private static final String URI_PATH = "action";


    private CallbackMatcher callbackMatcher;
    private List<SlackDispatcher> dispatchers;
    private String verificationToken;
    private Gson gson = GsonFactory.createSnakeCase();
    private UserContext userContext;

    @Autowired
    public SlackActionDispatcherController(
        CallbackMatcher callbackMatcher, @Value("${carassistant.slack.verification_token}") String verificationToken,
        List<SlackDispatcher> dispatchers,
        UserContext userContext) {
        this.callbackMatcher = callbackMatcher;
        this.dispatchers = dispatchers;
        this.verificationToken = verificationToken;
        this.userContext = userContext;
    }

    @RequestMapping(value = URI_PATH,
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object onReceiveAction(@RequestParam("payload") String rawPayload) throws IOException, SlackApiException {
        LOG.debug("rawPayload={}", rawPayload);
        ActionPayload payload;

        try {
            payload = gson.fromJson(rawPayload, ActionPayload.class);
        } catch (Exception e) {
            throw new CarAssistantException("Failed to parse payload: " + rawPayload, e);
        }

        if (!StringUtils.equals(verificationToken, payload.getToken())) {
            LOG.error("Wrong verification token in request: {}", payload.getToken());
            throw new SlackTokenValidationException("Wrong verification token in request: " + payload.getToken());
        }

        String type = payload.getType();
        Assert.hasLength(type, "'type' cannot be null or empty");

        String callbackId = payload.getCallbackId();
        LOG.debug("callbackId={}", callbackId);


        SlackDispatcher actionDispatcher = dispatchers.stream()
            .filter(dispatcher -> type.equalsIgnoreCase(dispatcher.getDispatchedType()))
            .findFirst()
            .orElseThrow(() -> {
                LOG.error("No dispatcher found for type={} payload={}", type, payload);
                return new SlackActionHandlerException(payload, "No dispatcher found for type=" + type);
            });

        HandlerExecutionWrapper handler = actionDispatcher.getHandlerFor(payload)
            .orElseThrow(() -> {
                LOG.error("No handler found for type={} callbackId={} actions={} payload={}", type, callbackId, payload.getActions(), payload);
                return new SlackActionHandlerException(payload, "No handler found for payload");
            });

        setupUserContext(payload);
        Object response = invokeHandlerMethod(handler, payload);
        if (response instanceof SlackApiResponse) {
            LOG.info("response={}", response);
            return null;
        }
        return response;
    }

    private void setupUserContext(ActionPayload payload) {
        Optional.ofNullable(payload.getUser())
            .ifPresent((user) -> userContext.setUserFromSlackIdIfExists(user.getId()));
    }

    private Object invokeHandlerMethod(HandlerExecutionWrapper handler, ActionPayload payload) {
        Method method = handler.getHandlerWrapper().getMethod();
        Object[] parameters = resolveMethodParameters(method, payload, handler.getCallbackVariables());
        try {
            return method.invoke(handler.getHandlerWrapper().getObject(), parameters);
        } catch (Exception e) {
            LOG.error("Failed to invoke handler {}", handler, e);
            throw new SlackActionHandlerException(payload, "Failed to invoke handler: " + handler, e);
        }
    }

    private Object[] resolveMethodParameters(Method method, ActionPayload payload, Map<String, String> callbackVariables) {
        Object[] params = new Object[method.getParameterCount()];
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < params.length; i++) {
            Class<?> paramType = paramTypes[i];

            if (ActionPayload.class.equals(paramType)) {
                params[i] = payload;
                continue;
            }
            if (Action.class.equals(paramType)) {
                params[i] = payload.getFirstAction();
                continue;
            }
            for (Annotation annotation : paramAnnotations[i]) {
                if (CallbackVariable.class.isInstance(annotation)) {
                    if(String.class.equals(paramType)) {
                        String name = (String) AnnotationUtils.getValue(annotation, "name");
                        params[i] = callbackVariables.get(name);
                    } else {
                        throw new IllegalStateException("Unsupported type for @CallbackVariable: " + paramType);
                    }
                }
            }
        }

        return params;
    }
}
