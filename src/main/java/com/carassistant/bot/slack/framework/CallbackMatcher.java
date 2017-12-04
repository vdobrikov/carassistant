package com.carassistant.bot.slack.framework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Component
public class CallbackMatcher extends AntPathMatcher {

    public CallbackMatcher(@Value("${framework.slack.callback_separator:/}") String separator,
                           @Value("${framework.slack.callback_is_case_sensitive:false}") boolean caseSensitive) {
        super();
        if (separator != null) {
            setPathSeparator(separator);
        }
        setCaseSensitive(caseSensitive);
    }
}
