package com.carbot.carbot.slack.controller;

import com.carbot.carbot.slack.SlackTokenValidationException;
import com.carbot.carbot.slack.model.ChallengeResponse;
import com.carbot.carbot.slack.model.Event;
import com.carbot.carbot.slack.model.EventRequest;
import com.carbot.carbot.slack.model.EventType;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@RestController
public class SlackEventController {
    private static final Logger LOG = LoggerFactory.getLogger(SlackEventController.class);

    private String verificationToken;
    private String slackBotToken;

    public SlackEventController(@Value("${slack.verification.token}") String verificationToken,
                                @Value("${slack.bot.access.token}") String slackBotToken) {
        this.verificationToken = verificationToken;
        this.slackBotToken = slackBotToken;
    }

    @RequestMapping(value = "event",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object onReceiveEvent(@RequestBody EventRequest request) throws IOException, SlackApiException {
        if (!StringUtils.equals(verificationToken, request.getToken())) {
            LOG.error("Wrong verification token in request: {}", request.getToken());
            throw new SlackTokenValidationException("Wrong verification token in request: " + request.getToken());
        }

        if (EventType.URL_VERIFICATION.equals(request.getType())) {
            LOG.info("URL verification");
            return new ChallengeResponse(request.getChallenge());
        }

        if (EventType.EVENT_CALLBACK.equals(request.getType())
            && request.getEvent() != null
            && EventType.MESSAGE.equals(request.getEvent().getType())) {

            String text = Optional.ofNullable(request.getEvent().getText()).orElse("");
            LOG.info("Message: {}", text);
            if (text.matches("(?i)^(share\\s+ride|ride\\s+share)$")) {
                SlackApiResponse response = replyWithRideSharingPromptTo(request.getEvent());
                LOG.info("response: {}", response);
            }
        }

        LOG.info("Skipping event: {}", request);
        return null;
    }

    private SlackApiResponse replyWithRideSharingPromptTo(Event event) throws IOException, SlackApiException {
        Slack slack = Slack.getInstance();
        return slack.methods().chatPostMessage(ChatPostMessageRequest.builder()
            .token(slackBotToken)
            .channel(event.getChannel())
            .attachments(
                Lists.newArrayList(
                    Attachment.builder()
                        .text("How can I help you?")
                        .fallback("This bot requires interactive messages support")
                        .callbackId("callback-initial-prompt")
                        .actions(
                            Lists.newArrayList(
                                Action.builder().name("initial-prompt").text("Share Ride").type(Action.Type.BUTTON).value("ride-share").build(),
                                Action.builder().name("initial-prompt").text("List Rides").type(Action.Type.BUTTON).value("ride-list").build(),
                                Action.builder().name("initial-prompt").text("Cancel").type(Action.Type.BUTTON).value("cancel").build()))
                        .build()))
            .build());
    }

}
