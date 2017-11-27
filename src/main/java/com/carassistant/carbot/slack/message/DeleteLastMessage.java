package com.carassistant.carbot.slack.message;

import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.github.seratch.jslack.api.methods.request.chat.ChatDeleteRequest;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class DeleteLastMessage {

    public static ChatDeleteRequest createRequest(String botAccessToken, ActionPayload payload) {
        return ChatDeleteRequest.builder()
            .token(botAccessToken)
            .channel(payload.getChannel().getId())
            .ts(payload.getOriginalMessage().getTs())
            .build();
    }
}
