package com.carassistant.carbot.slack.controller;

import com.carassistant.carbot.slack.SlackTokenValidationException;
import com.carassistant.carbot.slack.framework.UserContext;
import com.carassistant.carbot.slack.framework.model.ChallengeResponse;
import com.carassistant.carbot.slack.framework.model.Event;
import com.carassistant.carbot.slack.framework.model.EventRequest;
import com.carassistant.carbot.slack.framework.model.EventType;
import com.carassistant.carbot.slack.handler.ActionValue;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.InitialMessage;
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
    private String botAccessToken;
    private UserContext userContext;

    public SlackEventController(@Value("${slack.verification.token}") String verificationToken,
                                @Value("${slack.bot.access.token}") String botAccessToken,
                                UserContext userContext) {
        this.verificationToken = verificationToken;
        this.botAccessToken = botAccessToken;
        this.userContext = userContext;
    }

    @RequestMapping(value = "event",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object onReceiveEvent(@RequestBody EventRequest request) throws IOException, SlackApiException {
        LOG.debug("request={}", request);
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
            //if (text.matches("(?i)^(share\\s+ride|ride\\s+share)$")) {
            String userId = request.getEvent().getUser();
            if (userId != null) { // null = self
                setupUserContext(userId);
                SlackApiResponse response;
                if(!userContext.hasUser()) {
                    response = replyWithUserInitialPromptTo(request.getEvent());
                } else {
                    response = replyWithGenericInitialPromptTo(request.getEvent());
                }
                LOG.info("response: {}", response);
            }
        }

        LOG.info("Skipping event: {}", request);
        return null;
    }

    private void setupUserContext(String slackUserId) {
        userContext.setUserFromSlackIdIfExists(slackUserId);
    }

    private SlackApiResponse replyWithUserInitialPromptTo(Event event) throws IOException, SlackApiException {
        return Slack.getInstance().methods().chatPostMessage(ChatPostMessageRequest.builder()
            .token(botAccessToken)
            .channel(event.getChannel())
            .attachments(Lists.newArrayList(Attachment.builder()
                .callbackId(CallbackId.USER_INITIAL_PROMPT)
                .pretext("To proceed with this bot, your additional details are required")
                .actions(Lists.newArrayList(
                    Action.builder().name("user-initial-prompt").text("My Details").type(Action.Type.BUTTON).value(ActionValue.USER_DETAILS).build(),
                    Action.builder().name("user-initial-prompt").text("Cancel").type(Action.Type.BUTTON).value(ActionValue.CANCEL).build()))
                .build()))
            .build());
    }

    private SlackApiResponse replyWithGenericInitialPromptTo(Event event) throws IOException, SlackApiException {
        return Slack.getInstance().methods().chatPostMessage(
            InitialMessage.createMessage(botAccessToken, event.getChannel(), userContext.getUser()));
    }

}
