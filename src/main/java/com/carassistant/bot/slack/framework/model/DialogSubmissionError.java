package com.carassistant.bot.slack.framework.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DialogSubmissionError {
    private String name;
    private String error;

    public DialogSubmissionError() {
    }

    public DialogSubmissionError(String name, String error) {
        this.name = name;
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
