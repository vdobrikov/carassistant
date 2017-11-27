package com.carassistant.carbot.slack.framework.exception;

import java.lang.reflect.Method;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class SlackHandlerInitException extends Exception {
    private Class<?> handlerClass;
    private Method handlerMethod;

    public SlackHandlerInitException(Class<?> handlerClass, Method handlerMethod, String message) {
        super(message);
        this.handlerClass = handlerClass;
        this.handlerMethod = handlerMethod;
    }

    public SlackHandlerInitException(Class<?> handlerClass, Method handlerMethod, String message, Throwable cause) {
        super(message, cause);
        this.handlerClass = handlerClass;
        this.handlerMethod = handlerMethod;
    }

    public SlackHandlerInitException(Class<?> handlerClass, Method handlerMethod, Throwable cause) {
        super(cause);
        this.handlerClass = handlerClass;
        this.handlerMethod = handlerMethod;
    }

    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public Method getHandlerMethod() {
        return handlerMethod;
    }

    @Override
    public String getMessage() {
        return String.format("%s: handlerClass=%s, handlerMethod=%s", super.getMessage(), handlerClass, handlerMethod.getName());
    }
}
