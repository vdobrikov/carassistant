package com.carassistant.carbot.slack.controller;

import com.carassistant.carbot.slack.SlackTokenValidationException;
import com.carassistant.carbot.slack.framework.SlackApiClient;
import com.carassistant.carbot.slack.framework.UserContext;
import com.carassistant.carbot.slack.framework.model.ChallengeResponse;
import com.carassistant.carbot.slack.framework.model.EventRequest;
import com.carassistant.carbot.slack.framework.model.EventType;
import com.carassistant.carbot.slack.message.CommonRequests;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
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
    private UserContext userContext;
    private SlackApiClient slackApiClient;

    public SlackEventController(@Value("${slack.verification.token}") String verificationToken,
                                UserContext userContext,
                                SlackApiClient slackApiClient) {
        this.verificationToken = verificationToken;
        this.userContext = userContext;
        this.slackApiClient = slackApiClient;
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
            LOG.info("text={}", text);
            String userId = request.getEvent().getUser();
            if (userId != null) { // null = self
                setupUserContext(userId);
                SlackApiResponse response = replyToMessage(request.getEvent().getChannel(), text);
                LOG.info("response={}", response);
                return response;
            }
        }

        LOG.info("Skipping event: {}", request);
        return null;
    }

    private SlackApiResponse replyToMessage(String channelId, String text) throws IOException, SlackApiException {
        if (text.matches("(?i)^user$")) {
            return slackApiClient.send(CommonRequests.initialMessageUser(channelId));
        } else if (!userContext.hasUser()) {
            return slackApiClient.send(ChatPostMessageRequest.builder()
                .channel(channelId)
                .text("To proceed with this bot, your additional details are required. Please provide initial information with `user` command")
                .build());
        }
        if (text.matches("(?i)^ride$")) {
            return slackApiClient.send(CommonRequests.initialMessageRide(channelId));
        }
        return slackApiClient.send(ChatPostMessageRequest.builder()
            .channel(channelId)
            .text("Unknown command.\n" +
                "Please try one of following:\n" +
                "`user` - user options\n" +
                "`ride` - rides options\n" +
                "more to come...")
            .build());
    }

    private void setupUserContext(String slackUserId) {
        userContext.setUserFromSlackIdIfExists(slackUserId);
    }
}
