package com.carassistant.carbot.slack.framework.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventRequest {
    private String type;
    private String token;
    private String challenge;
    private Event event;
    @JsonProperty("api_app_id")
    private String apiAppId;
    @JsonProperty("event_id")
    private String eventId;
    @JsonProperty("event_time")
    private long eventTime;
    @JsonProperty("authed_users")
    private List<String> authedUers;

    public String getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getApiAppId() {
        return apiAppId;
    }

    public String getEventId() {
        return eventId;
    }

    public long getEventTime() {
        return eventTime;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "EventRequest{" +
            "type='" + type + '\'' +
            ", token='" + token + '\'' +
            ", challenge='" + challenge + '\'' +
            ", event=" + event +
            ", apiAppId='" + apiAppId + '\'' +
            ", eventId='" + eventId + '\'' +
            ", eventTime=" + eventTime +
            ", authedUers=" + authedUers +
            '}';
    }
}
