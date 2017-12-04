package com.carassistant.bot.slack.framework.dispatcher;

import java.util.Map;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class HandlerExecutionWrapper {
    private HandlerWrapper handlerWrapper;
    private String callback;
    private String callbackPattern;
    private Map<String, String> callbackVariables;

    public HandlerExecutionWrapper(HandlerWrapper handlerWrapper, String callback, String callbackPattern, Map<String, String> callbackVariables) {
        this.handlerWrapper = handlerWrapper;
        this.callback = callback;
        this.callbackPattern = callbackPattern;
        this.callbackVariables = callbackVariables;
    }

    public HandlerWrapper getHandlerWrapper() {
        return handlerWrapper;
    }

    public String getCallback() {
        return callback;
    }

    public String getCallbackPattern() {
        return callbackPattern;
    }

    public Map<String, String> getCallbackVariables() {
        return callbackVariables;
    }
}
