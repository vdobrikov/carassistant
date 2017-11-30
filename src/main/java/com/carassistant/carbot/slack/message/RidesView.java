package com.carassistant.carbot.slack.message;

import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.handler.ActionName;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RidesView {

    public static ChatPostMessageRequest createPostRequest(String callbackId, ActionPayload payload, Page<Ride> ridesPage, User user) {
        return ChatPostMessageRequest.builder()
            .channel(payload.getChannel().getId())
            .attachments(createAttachmentsFor(callbackId, user, ridesPage))
            .build();
    }

    public static ChatUpdateRequest createUpdateRequest(String callbackId, ActionPayload payload, Page<Ride> ridesPage, User user) {
        return ChatUpdateRequest.builder()
            .channel(payload.getChannel().getId())
            .ts(payload.getMessageTs())
            .attachments(createAttachmentsFor(callbackId, user, ridesPage))
            .build();
    }

    public static List<Attachment> createAttachmentsFor(String callbackId, User user, Page<Ride> ridesPage) {
        List<Attachment> attachments = createAttachmentsFor(callbackId, user, ridesPage.getContent());

        if (ridesPage.hasNext() || ridesPage.hasPrevious()) {
            attachments.add(Attachment.builder()
                .callbackId(callbackId)
                .pretext("Navigation")
                .actions(createNavigationActionsFor(ridesPage))
                .build());
        }
        return attachments;
    }

    public static List<Attachment> createAttachmentsFor(String callbackId, User user, List<Ride> rides) {
        return rides.stream()
            .map(ride -> RideView.asAttachmentBuilder(ride)
                .callbackId(callbackId)
                .pretext(String.format("Shared by <@%s>", ride.getOwner().getSlackInfo().getUserId()))
                .actions(RideView.createActionsFor(ride, user))
                .build())
            .collect(Collectors.toList());
    }

    private static List<Action> createNavigationActionsFor(Page<Ride> ridesPage) {
        List<Action> actions = new ArrayList<>();
        if (ridesPage.hasPrevious()) {
            actions.add(Action.builder()
                .name(ActionName.PREV)
                .text("Prev")
                .type(Action.Type.BUTTON)
                .value(String.valueOf(ridesPage.getNumber()))
                .build());
        }
        if (ridesPage.hasNext()) {
            actions.add(Action.builder()
                .name(ActionName.NEXT)
                .text("Next")
                .type(Action.Type.BUTTON)
                .value(String.valueOf(ridesPage.getNumber()))
                .build());
        }
        return actions;
    }

}
