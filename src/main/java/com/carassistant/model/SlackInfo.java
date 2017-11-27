package com.carassistant.model;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class SlackInfo {
    private String userId;
    private String channelId;

    public SlackInfo() {
    }

    public SlackInfo(String userId, String channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SlackInfo slackInfo = (SlackInfo) o;

        if (!userId.equals(slackInfo.userId)) return false;
        return channelId.equals(slackInfo.channelId);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + channelId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SlackInfo{" +
            "userId='" + userId + '\'' +
            ", channelId='" + channelId + '\'' +
            '}';
    }
}
