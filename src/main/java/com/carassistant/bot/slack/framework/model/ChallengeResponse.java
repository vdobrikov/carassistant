package com.carassistant.bot.slack.framework.model;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class ChallengeResponse {
    private String challenge;

    public ChallengeResponse(String challenge) {
        this.challenge = challenge;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }
}
