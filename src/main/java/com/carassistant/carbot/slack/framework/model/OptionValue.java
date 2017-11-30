package com.carassistant.carbot.slack.framework.model;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class OptionValue {
    private String value;

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "OptionValue{" +
            "value='" + value + '\'' +
            '}';
    }
}
