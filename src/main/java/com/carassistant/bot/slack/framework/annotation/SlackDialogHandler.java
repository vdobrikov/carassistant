package com.carassistant.bot.slack.framework.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlackDialogHandler {

    @AliasFor("callbackId")
    String value() default "";

    @AliasFor("value")
    String callbackId() default "";
}
