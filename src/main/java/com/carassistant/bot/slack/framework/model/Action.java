package com.carassistant.bot.slack.framework.model;

import com.google.gson.annotations.SerializedName;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class Action {
    public enum Type {

        /**
         * @see <a href="https://api.slack.com/docs/message-buttons">Message button</a>
         */
        @SerializedName("button")
        BUTTON ("button"),

        /**
         * @see <a href="https://api.slack.com/docs/message-menus">Message menus</a>
         */
        @SerializedName("select")
        SELECT ("select");

        private final String value;
        Type(String value) {
            this.value = value;
        }

        public String value() { return value; }
    }

    private String name;
    private String value;
    private List<OptionValue> selectedOptions;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public List<OptionValue> getSelectedOptions() {
        return selectedOptions;
    }

    public OptionValue getFirstSelectedOption() {
        Assert.notEmpty(getSelectedOptions(), "'selected_options' is empty");
        return getSelectedOptions().get(0);
    }

    @Override
    public String toString() {
        return "Action{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", selectedOptions=" + selectedOptions +
            '}';
    }
}