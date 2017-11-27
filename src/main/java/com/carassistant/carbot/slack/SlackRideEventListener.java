package com.carassistant.carbot.slack;

import com.carassistant.event.ride.RideCompanionEvent;
import com.carassistant.event.ride.RideLifecycleEvent;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.carbot.slack.message.RideView;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.model.Attachment;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Component
public class SlackRideEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(SlackRideEventListener.class);

    private String botAccessToken;

    @Autowired
    public SlackRideEventListener(@Value("${slack.bot.access.token}") String botAccessToken) {
        this.botAccessToken = botAccessToken;
    }

    @EventListener
    private void onRideCompanionEvent(RideCompanionEvent event) throws IOException, SlackApiException {
        Ride ride = event.getRide();
        User owner = ride.getOwner();
        User companion = event.getCompanion();
        sendRideAwareMessage(String.format("<@%s> has %s your ride", companion.getSlackInfo().getUserId(),
            event.getAction() == RideCompanionEvent.Action.JOIN ? "joined" : "unjoined"), ride, owner);
    }

    @EventListener
    private void onRideLifecycleEvent(RideLifecycleEvent event) {
        if (event.getNewStatus() == Ride.Status.CANCELLED) {
            sendAlertToCompanions(event.getRide());
        }
    }

    private void sendAlertToCompanions(Ride ride) {
        Set<User> uniqueCompanions = new HashSet<>(ride.getCompanions());
        for (User user : uniqueCompanions) {
            if (!user.equals(ride.getOwner())) {
                try {
                    sendRideAwareMessage(String.format("Unfortunately <@%s> has cancelled this ride", ride.getOwner().getSlackInfo().getUserId()), ride, user);
                } catch (SlackApiException | IOException e) {
                    LOG.error("Failed to send cancellation to companion ride={} companion={}", ride, user, e);
                }
            }
        }
    }

    private ChatPostMessageResponse sendRideAwareMessage(String text, Ride ride, User user) throws IOException, SlackApiException {
        return Slack.getInstance().methods().chatPostMessage(ChatPostMessageRequest.builder()
            .token(botAccessToken)
            .channel(user.getSlackInfo().getChannelId())
            .text(text)
            .attachments(Lists.newArrayList(Attachment.builder()
                .fields(RideView.asFields(ride))
                .build()))
            .build());
    }


}
