package com.carassistant.bot.slack.framework.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DialogSubmissionErrorsResponse {
    private List<DialogSubmissionError> errors;

    public DialogSubmissionErrorsResponse() {
    }

    public DialogSubmissionErrorsResponse(List<DialogSubmissionError> errors) {
        this.errors = errors;
    }

    public List<DialogSubmissionError> getErrors() {
        return errors;
    }

    public void setErrors(List<DialogSubmissionError> errors) {
        this.errors = errors;
    }
}
