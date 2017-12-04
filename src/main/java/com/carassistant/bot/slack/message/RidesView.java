package com.carassistant.bot.slack.message;

import com.carassistant.bot.slack.handler.ActionName;
import com.carassistant.bot.slack.handler.CallbackId;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.bot.slack.framework.model.ActionPayload;
import com.carassistant.utilities.SlackTextHelper;
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

    public static String createCallbackFor(Page<Ride> ridesPage) {
        return String.format(CallbackId.RIDE_LIST.replaceAll("\\{.+?\\}", "%s"), ridesPage.getNumber());
    }

    public static ChatPostMessageRequest createPostRequest(ActionPayload payload, Page<Ride> ridesPage, User user) {
        return ChatPostMessageRequest.builder()
            .channel(payload.getChannel().getId())
            .attachments(createAttachmentsFor(user, ridesPage))
            .build();
    }

    public static ChatUpdateRequest createUpdateRequest(ActionPayload payload, Page<Ride> ridesPage, User user) {
        return ChatUpdateRequest.builder()
            .channel(payload.getChannel().getId())
            .ts(payload.getMessageTs())
            .attachments(createAttachmentsFor(user, ridesPage))
            .build();
    }

    public static List<Attachment> createAttachmentsFor(User user, Page<Ride> ridesPage) {
        List<Attachment> attachments = createAttachmentsFor(user, ridesPage.getContent());

        if (ridesPage.hasNext() || ridesPage.hasPrevious()) {
            attachments.add(Attachment.builder()
                .callbackId(createCallbackFor(ridesPage))
                .pretext("Navigation")
                .actions(createNavigationActionsFor(ridesPage))
                .build());
        }
        return attachments;
    }

    public static List<Attachment> createAttachmentsFor(User user, List<Ride> rides) {
        return rides.stream()
            .map(ride -> RideView.asAttachmentBuilder(ride)
                .callbackId(RideView.createCallbackFor(ride))
                .pretext(String.format("Shared by %s", SlackTextHelper.userMentionFrom(ride.getOwner().getSlackInfo().getUserId())))
                .actions(RideView.createActionsFor(ride, user))
                .build())
            .collect(Collectors.toList());
    }

    private static List<Action> createNavigationActionsFor(Page<Ride> ridesPage) {
        List<Action> actions = new ArrayList<>();
        if (ridesPage.hasPrevious()) {
            actions.add(Action.builder()
                .type(Action.Type.BUTTON)
                .name(ActionName.PREV)
                .text("Prev")
                .build());
        }
        if (ridesPage.hasNext()) {
            actions.add(Action.builder()
                .type(Action.Type.BUTTON)
                .name(ActionName.NEXT)
                .text("Next")
                .build());
        }
        return actions;
    }

}
