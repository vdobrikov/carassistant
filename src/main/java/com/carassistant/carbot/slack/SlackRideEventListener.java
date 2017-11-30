package com.carassistant.carbot.slack;

import com.carassistant.carbot.slack.framework.SlackApiClient;
import com.carassistant.carbot.slack.message.RideView;
import com.carassistant.event.ride.RideCompanionEvent;
import com.carassistant.event.ride.RideDepartureDateChangedEvent;
import com.carassistant.event.ride.RideLifecycleEvent;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.model.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Component
public class SlackRideEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(SlackRideEventListener.class);

    private SlackApiClient slackApiClient;

    @Autowired
    public SlackRideEventListener(SlackApiClient slackApiClient) {
        this.slackApiClient = slackApiClient;
    }

    @EventListener
    private void onRideCompanionEvent(RideCompanionEvent event) throws IOException, SlackApiException {
        Ride ride = event.getRide();
        User owner = ride.getOwner();
        User companion = event.getCompanion();
        if (!owner.equals(companion)) {
            sendRideAwareMessage(ride, owner, String.format("<@%s> has %s your ride", companion.getSlackInfo().getUserId(),
                event.getAction() == RideCompanionEvent.Action.JOIN ? "joined" : "unjoined"));
        }
    }

    @EventListener
    private void onRideLifecycleEvent(RideLifecycleEvent event) {
        if (event.getNewStatus() == Ride.Status.CANCELLED) {
            Ride ride = event.getRide();
            sendAlertToCompanions(ride, String.format("Unfortunately <@%s> has cancelled this ride", ride.getOwner().getSlackInfo().getUserId()));
        }
    }

    @EventListener
    private void onRideDepartureDateChangedEventEvent(RideDepartureDateChangedEvent event) {
        Ride ride = event.getRide();
        sendAlertToCompanions(ride, String.format("<@%s> has changed departure time", ride.getOwner().getSlackInfo().getUserId()));
    }

    private void sendAlertToCompanions(Ride ride, String text) {
        Set<User> uniqueCompanions = new HashSet<>(ride.getCompanions());
        uniqueCompanions.remove(ride.getOwner());
        for (User user : uniqueCompanions) {
            try {
                sendRideAwareMessage(ride, user, text);
            } catch (SlackApiException | IOException e) {
                LOG.error("Failed to send cancellation to companion ride={} companion={}", ride, user, e);
            }
        }
    }

    private ChatPostMessageResponse sendRideAwareMessage(Ride ride, User user, String text) throws IOException, SlackApiException {
        return slackApiClient.send(ChatPostMessageRequest.builder()
            .channel(user.getSlackInfo().getChannelId())
            .text(text)
            .attachments(newArrayList(Attachment.builder()
                .fields(RideView.asFields(ride))
                .actions(RideView.createActionsFor(ride, user))
                .build()))
            .build());
    }


}
