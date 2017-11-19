package com.carbot.carbot.slack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    private String type;
    private String user;
    private String channel;
    private String text;
    @JsonProperty("event_ts")
    private String eventTs;
    private String ts;

    public String getType() {
        return type;
    }

    public String getUser() {
        return user;
    }

    public String getChannel() {
        return channel;
    }

    public String getText() {
        return text;
    }

    public String getEventTs() {
        return eventTs;
    }

    public String getTs() {
        return ts;
    }

    @Override
    public String toString() {
        return "Event{" +
            "type='" + type + '\'' +
            ", user='" + user + '\'' +
            ", channel='" + channel + '\'' +
            ", text='" + text + '\'' +
            ", eventTs='" + eventTs + '\'' +
            ", ts='" + ts + '\'' +
            '}';
    }
}
