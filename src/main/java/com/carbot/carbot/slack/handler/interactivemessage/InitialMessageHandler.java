package com.carbot.carbot.slack.handler.interactivemessage;

import com.carbot.carbot.slack.annotation.SlackHandler;
import com.carbot.carbot.slack.annotation.SlackInteractiveMessageActionHandler;
import com.carbot.carbot.slack.handler.ActionValue;
import com.carbot.carbot.slack.handler.CallbackId;
import com.carbot.carbot.slack.model.ActionPayload;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatDeleteRequest;
import com.github.seratch.jslack.api.methods.request.dialog.DialogOpenRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatDeleteResponse;
import com.github.seratch.jslack.api.methods.response.dialog.DialogOpenResponse;
import com.github.seratch.jslack.api.model.dialog.Dialog;
import com.github.seratch.jslack.api.model.dialog.DialogSubType;
import com.github.seratch.jslack.api.model.dialog.DialogTextElement;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler(callbackId = CallbackId.INITIAL_PROMPT)
public class InitialMessageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(InitialMessageHandler.class);

    private String apiBotToken;

    public InitialMessageHandler(@Value("${slack.bot.access.token}") String apiBotToken) {
        this.apiBotToken = apiBotToken;
    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.CANCEL)
    public void onCancel(ActionPayload payload) throws IOException, SlackApiException {
        ChatDeleteResponse chatDeleteResponse = Slack.getInstance().methods().chatDelete(ChatDeleteRequest.builder()
            .token(apiBotToken)
            .channel(payload.getChannel().getId())
            .ts(payload.getOriginalMessage().getTs())
            .build());
        LOG.info("chatDeleteResponse={}", chatDeleteResponse);
    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.RIDE_SHARE)
    public void onRideShare(ActionPayload payload) throws IOException, SlackApiException {
        DialogTextElement departurePointElement = DialogTextElement.builder()
            .name("departurePoint")
            .label("Departure Point")
            .hint("Your departure point")
            .placeholder("Bunina, 1")
            .value("office")
            .minLength(1)
            .maxLength(100)
            .build();
        DialogTextElement destinationPointElement = DialogTextElement.builder()
            .name("destinationPoint")
            .label("Destination Point")
            .hint("Where are you going?")
            .placeholder("Deribasovskaya, 1")
            .minLength(1)
            .maxLength(100)
            .build();
        DialogTextElement departureTimeElement = DialogTextElement.builder()
            .name("departureTime")
            .label("Departure Time")
            .hint("What time are you leaving?")
            .placeholder("7pm")
            .minLength(1)
            .maxLength(100)
            .build();
        DialogTextElement freeSeatsElement = DialogTextElement.builder()
            .subtype(DialogSubType.NUMBER)
            .name("freeSeats")
            .label("Free Seats")
            .hint("How many friends can join?")
            .placeholder("3")
            .value("3")
            .minLength(1)
            .maxLength(3)
            .build();
        DialogTextElement commentElement = DialogTextElement.builder()
            .name("comment")
            .label("Comment")
            .hint("Any additional information")
            .minLength(1)
            .maxLength(100)
            .optional(true)
            .build();

        Dialog dialog = Dialog.builder()
            .title("Ride Sharing")
            .callbackId(CallbackId.SHARE_RIDE_DIALOG)
            .elements(Lists.newArrayList(
                departurePointElement,
                destinationPointElement,
                departureTimeElement,
                freeSeatsElement,
                commentElement))
            .submitLabel("Share")
            .build();
        LOG.info("dialog={}", dialog);

        DialogOpenResponse response = Slack.getInstance().methods().dialogOpen(DialogOpenRequest.builder()
            .token(apiBotToken)
            .triggerId(payload.getTriggerId())
            .dialog(dialog)
            .build());

        LOG.info("response={}", response);
    }
}
