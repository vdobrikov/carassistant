package com.carassistant.utilities;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class SlackTextHelper {
    public static String userMentionFrom(String slackUserId) {
        return String.format("Joined to <@%s>'s ride", slackUserId);
    }
}
