package com.carassistant.carbot.slack.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlackInteractiveMessageActionHandler {
    String callbackId() default "";
    String[] actionValue() default "";
}
