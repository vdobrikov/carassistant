package com.carbot.carbot.slack.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface SlackInteractiveMessageActionHandler {
    String callbackId() default "";
    String actionValue() default "";
}
