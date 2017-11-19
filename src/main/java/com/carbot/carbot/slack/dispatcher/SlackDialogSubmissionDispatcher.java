package com.carbot.carbot.slack.dispatcher;

import com.carbot.carbot.slack.annotation.SlackDialogSubmissionHandler;
import com.carbot.carbot.slack.annotation.SlackHandler;
import com.carbot.carbot.slack.exception.SlackHandlerInitException;
import com.carbot.carbot.slack.model.ActionPayload;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@Component
public class SlackDialogSubmissionDispatcher extends SlackAbstractDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(SlackDialogSubmissionDispatcher.class);

    private ApplicationContext applicationContext;
    private Map<String, HandlerWrapper> callbacksHandlers;

    @Autowired
    public SlackDialogSubmissionDispatcher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init() throws SlackHandlerInitException {
        Map<String, HandlerWrapper> callbacksHandlers = new HashMap<>();

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(SlackHandler.class);
        for (Object rawHandler : beans.values()) {
            Class<?> handlerClass = rawHandler.getClass();
            SlackHandler classAnnotation = AnnotationUtils.getAnnotation(handlerClass, SlackHandler.class);
            String classCallbackId = (String) AnnotationUtils.getValue(classAnnotation, "callbackId");

            for (Method method : handlerClass.getMethods()) {
                SlackDialogSubmissionHandler methodAnnotation = AnnotationUtils.getAnnotation(method, SlackDialogSubmissionHandler.class);
                if (methodAnnotation == null) {
                    continue;
                }

                Parameter[] parameters = method.getParameters();
                if (parameters.length != 1) {
                    throw new SlackHandlerInitException(handlerClass, method, "Handler method can have only one parameter");
                }
                if (!method.getParameters()[0].getType().equals(ActionPayload.class)) {
                    throw new SlackHandlerInitException(handlerClass, method, String.format("Handler parameter should have '%s' type", ActionPayload.class.getCanonicalName()));
                }

                String methodCallbackId = (String) AnnotationUtils.getValue(methodAnnotation, "callbackId");

                String callbackId = StringUtils.defaultIfEmpty(methodCallbackId, classCallbackId);
                if (StringUtils.isBlank(callbackId)) {
                    throw new SlackHandlerInitException(handlerClass, method, "'callbackId' cannot be null or empty");
                }

                HandlerWrapper registeredHandler = callbacksHandlers.get(methodCallbackId);
                if(registeredHandler != null) {
                    throw new SlackHandlerInitException(handlerClass, method,
                        String.format("Callback '%s' already registered as %s",
                            callbackId, registeredHandler));
                }
                HandlerWrapper handler = new HandlerWrapper(rawHandler, method);
                callbacksHandlers.put(callbackId, handler);
                LOG.debug("Handler registered: {}: {}", callbackId, handler);
            }
        }
        this.callbacksHandlers = ImmutableMap.copyOf(callbacksHandlers);
    }

    @Override
    public Optional<HandlerWrapper> getHandlerFor(ActionPayload payload) {
        validate(payload);
        return Optional.ofNullable(getHandlerFor(payload.getCallbackId()));
    }

    private HandlerWrapper getHandlerFor(String callback) {
        return callbacksHandlers.get(callback);
    }

    @Override
    public String getDispatchedType() {
        return "dialog_submission";
    }
}
