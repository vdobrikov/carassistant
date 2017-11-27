package com.carassistant.carbot.slack.framework.dispatcher;

import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.annotation.SlackInteractiveMessageActionHandler;
import com.carassistant.carbot.slack.framework.exception.SlackHandlerInitException;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@Component
public class SlackInteractiveMessageActionDispatcher extends SlackAbstractDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(SlackInteractiveMessageActionDispatcher.class);

    private ApplicationContext applicationContext;
    private Table<String, String, HandlerWrapper> callbacksActionsHandlers;

    @Autowired
    public SlackInteractiveMessageActionDispatcher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init() throws SlackHandlerInitException {
        Table<String, String, HandlerWrapper> callbacksActionsHandlers = HashBasedTable.create();

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(SlackHandler.class);
        for (Object rawHandler : beans.values()) {
            // With AOP
            Class<?> handlerClass = AopUtils.getTargetClass(rawHandler);
            // Without AOP
            // Class<?> handlerClass = rawHandler.getClass();
            SlackHandler classAnnotation = AnnotationUtils.getAnnotation(handlerClass, SlackHandler.class);
            String classCallbackId = (String) AnnotationUtils.getValue(classAnnotation, "callbackId");

            for (Method method : handlerClass.getMethods()) {
                SlackInteractiveMessageActionHandler methodAnnotation = AnnotationUtils.getAnnotation(method, SlackInteractiveMessageActionHandler.class);
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
                if (StringUtils.isBlank(callbackId)) {
                    throw new SlackHandlerInitException(handlerClass, method, "'callbackId' cannot be null or empty");
                }

                Object rawActionValue = AnnotationUtils.getValue(methodAnnotation, "actionValue");
                String[] actionValues = (String[]) rawActionValue;

                if (actionValues.length == 0) {
                    throw new SlackHandlerInitException(handlerClass, method, "'actionValue' cannot be empty array");
                }
                for (String actionValue : actionValues) {
                    if (StringUtils.isBlank(actionValue)) {
                        throw new SlackHandlerInitException(handlerClass, method, "'actionValue' cannot be null or empty");
                    }

                    HandlerWrapper registeredHandler = callbacksActionsHandlers.get(callbackId, actionValue);
                    if (registeredHandler != null) {
                        throw new SlackHandlerInitException(handlerClass, method,
                            String.format("Callback:action '%s:%s' already registered as %s",
                                callbackId, actionValue, registeredHandler));
                    }
                    HandlerWrapper handler = new HandlerWrapper(rawHandler, method);
                    callbacksActionsHandlers.put(callbackId, actionValue, handler);
                    LOG.debug("Handler registered: {}:{}: {}", callbackId, actionValue, handler);
                }
            }
        }
        this.callbacksActionsHandlers = ImmutableTable.copyOf(callbacksActionsHandlers);
    }

    @Override
    public Optional<HandlerWrapper> getHandlerFor(ActionPayload payload) {
        validate(payload);
        return Optional.ofNullable(getHandlerFor(payload.getCallbackId(), payload.getActions().get(0).getValue()));
    }

    private HandlerWrapper getHandlerFor(String callback, String action) {
        return callbacksActionsHandlers.get(callback, action);
    }

    @Override
    public String getDispatchedType() {
        return "interactive_message";
    }
}
