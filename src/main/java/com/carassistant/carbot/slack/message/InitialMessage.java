package com.carassistant.carbot.slack.message;

import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.model.User;
import com.carassistant.carbot.slack.handler.ActionValue;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;
import com.google.common.collect.Lists;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class InitialMessage {
    public static final String CALLBACK = CallbackId.GENERIC_INITIAL_PROMPT;

    public static ChatPostMessageRequest createMessage(String botAccessToken, String channelId, User user) {
        return ChatPostMessageRequest.builder()
            .token(botAccessToken)
            .channel(channelId)
            .attachments(
                Lists.newArrayList(
                    Attachment.builder()
                        .callbackId(CALLBACK)
                        .pretext("How can I help you?")
                        .actions(
                            Lists.newArrayList(
                                Action.builder().name("initial-prompt").text("Share Ride").type(Action.Type.BUTTON).value(ActionValue.RIDE_SHARE).build(),
                                Action.builder().name("initial-prompt").text("List Rides").type(Action.Type.BUTTON).value(ActionValue.RIDE_LIST).build(),
                                Action.builder().name("initial-prompt").text("List My Rides").type(Action.Type.BUTTON).value(ActionValue.RIDE_LIST_MY).build(),
                                Action.builder().name("initial-prompt").text("Cancel").type(Action.Type.BUTTON).value(ActionValue.CANCEL).build()))
                        .build()))
            .build();
    }
}
