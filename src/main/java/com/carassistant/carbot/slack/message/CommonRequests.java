package com.carassistant.carbot.slack.message;

import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.handler.ActionName;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.github.seratch.jslack.api.methods.request.chat.ChatDeleteRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class CommonRequests {

    public static ChatDeleteRequest deleteOriginalMessage(ActionPayload payload) {
        return ChatDeleteRequest.builder()
            .channel(payload.getChannel().getId())
            .ts(payload.getOriginalMessage().getTs())
            .build();
    }

    public static ChatUpdateRequest updateOriginalMessage(ActionPayload payload, String text) {
        return updateOriginalMessage(payload, text, newArrayList());
    }

    public static ChatUpdateRequest updateOriginalMessage(ActionPayload payload, String text, List<Attachment> attachments) {
        return ChatUpdateRequest.builder()
            .channel(payload.getChannel().getId())
            .ts(payload.getOriginalMessage().getTs())
            .text(text)
            .attachments(attachments)
            .build();
    }

    public static ChatPostMessageRequest initialMessageRide(String channelId) {
        return initialMessageRide(channelId, "Ride options");
    }

    public static ChatPostMessageRequest initialMessageRide(String channelId, String text) {
        return ChatPostMessageRequest.builder()
            .channel(channelId)
            .attachments(
                newArrayList(
                    Attachment.builder()
                        .callbackId(CallbackId.RIDE_INITIAL_PROMPT)
                        .pretext(text)
                        .actions(
                            newArrayList(
                                Action.builder().value("initial-prompt").type(Action.Type.BUTTON).text("Share Ride").name(ActionName.RIDE_SHARE).build(),
                                Action.builder().value("initial-prompt").type(Action.Type.BUTTON).text("List Rides").name(ActionName.RIDE_LIST).build(),
                                Action.builder().value("initial-prompt").type(Action.Type.BUTTON).text("List My Rides").name(ActionName.RIDE_LIST_MY).build(),
                                Action.builder().value("initial-prompt").type(Action.Type.BUTTON).text("Cancel").name(ActionName.CANCEL).build()))
                        .build()))
            .build();
    }

    public static ChatPostMessageRequest initialMessageUser(String channelId) {
        return initialMessageUser(channelId, "User options");
    }

    public static ChatPostMessageRequest initialMessageUser(String channelId, String text) {
        return ChatPostMessageRequest.builder()
            .channel(channelId)
            .attachments(newArrayList(Attachment.builder()
                .callbackId(CallbackId.USER_INITIAL_PROMPT)
                .pretext(text)
                .actions(newArrayList(
                    Action.builder().type(Action.Type.BUTTON).value("user-initial-prompt").text("My Details").name(ActionName.USER_DETAILS).build(),
                    Action.builder().type(Action.Type.BUTTON).value("user-initial-prompt").text("Cancel").name(ActionName.CANCEL).build()))
                .build()))
            .build();
    }
}
