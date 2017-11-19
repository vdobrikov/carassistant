package com.carbot.carbot.slack.dispatcher;

import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class HandlerWrapper {
    private Object object;
    private Method method;

    public HandlerWrapper(Object object, Method method) {
        Assert.notNull(object, "'object' cannot be null");
        Assert.notNull(method, "'method' cannot be null");
        this.object = object;
        this.method = method;
    }

    public Object getObject() {
        return object;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "HandlerWrapper{" +
            "object=" + object +
            ", method=" + method.getName() +
            '}';
    }
}
