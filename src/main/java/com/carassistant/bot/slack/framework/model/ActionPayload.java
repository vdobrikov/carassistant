package com.carassistant.bot.slack.framework.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.seratch.jslack.api.model.Channel;
import com.github.seratch.jslack.api.model.Message;
import com.github.seratch.jslack.api.model.Team;
import com.github.seratch.jslack.api.model.User;
import com.google.gson.annotations.SerializedName;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionPayload {
    private List<Action> actions;
    @SerializedName("callback_id")
    @JsonProperty("callback_id")
    private String callbackId;
    private Team team;
    private Channel channel;
    private User user;
    @SerializedName("action_ts")
    @JsonProperty("action_ts")
    private String actionTs;
    @SerializedName("message_ts")
    @JsonProperty("message_ts")
    private String messageTs;
    @SerializedName("attachment_id")
    @JsonProperty("attachment_id")
    private String attachmentId;
    private String token;
    @SerializedName("is_app_unfurl")
    @JsonProperty("is_app_unfurl")
    private boolean isAppUnfurl;
    private String type;
    @SerializedName("original_message")
    @JsonProperty("original_message")
    private Message originalMessage;
    @SerializedName("response_url")
    @JsonProperty("response_url")
    private String responseUrl;
    @SerializedName("trigger_id")
    @JsonProperty("trigger_id")
    private String triggerId;
    private Map<String, String> submission;

    public Map<String, String> getSubmission() {
        return submission;
    }

    public void setSubmission(Map<String, String> submission) {
        this.submission = submission;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setActionTs(String actionTs) {
        this.actionTs = actionTs;
    }

    public void setMessageTs(String messageTs) {
        this.messageTs = messageTs;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAppUnfurl(boolean appUnfurl) {
        isAppUnfurl = appUnfurl;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOriginalMessage(Message originalMessage) {
        this.originalMessage = originalMessage;
    }

    public void setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Action getFirstAction() {
        Assert.notEmpty(getActions(), "'actions' is empty");
        return getActions().get(0);
    }

    public String getCallbackId() {
        return callbackId;
    }

    public Team getTeam() {
        return team;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public String getActionTs() {
        return actionTs;
    }

    public String getMessageTs() {
        return messageTs;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public String getToken() {
        return token;
    }

    public boolean isAppUnfurl() {
        return isAppUnfurl;
    }

    public String getType() {
        return type;
    }

    public Message getOriginalMessage() {
        return originalMessage;
    }

    public String getResponseUrl() {
        return responseUrl;
    }

    public String getTriggerId() {
        return triggerId;
    }

    @Override
    public String toString() {
        return "ActionPayload{" +
            "actions=" + actions +
            ", callbackId='" + callbackId + '\'' +
            ", team=" + team +
            ", channel=" + channel +
            ", user=" + user +
            ", actionTs='" + actionTs + '\'' +
            ", messageTs='" + messageTs + '\'' +
            ", attachmentId='" + attachmentId + '\'' +
            ", token='" + token + '\'' +
            ", isAppUnfurl=" + isAppUnfurl +
            ", type='" + type + '\'' +
            ", originalMessage=" + originalMessage +
            ", responseUrl='" + responseUrl + '\'' +
            ", triggerId='" + triggerId + '\'' +
            ", submission=" + submission +
            '}';
    }
}
