package com.carassistant.carbot.slack;

import com.carassistant.model.Ride;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.model.User;
import com.carassistant.service.RideService;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Component
public class SlackRideScheduledTasks {
    private static final Logger LOG = LoggerFactory.getLogger(SlackRideScheduledTasks.class);

    private String botAccessToken;
    private int rideOwnerConfirmationAlertTime;
    private int rideCompanionsConfirmationAlertTime;
    private RideService rideService;

    @Autowired
    public SlackRideScheduledTasks(@Value("${slack.bot.access.token}") String botAccessToken,
                                   @Value("${ride.confirmation.owner.alert.time.minutes}") int rideOwnerConfirmationAlertTime,
                                   @Value("${ride.confirmation.companions.alert.time.minutes}") int rideCompanionsConfirmationAlertTime,
                                   RideService rideService) {
        this.botAccessToken = botAccessToken;
        this.rideOwnerConfirmationAlertTime = rideOwnerConfirmationAlertTime;
        this.rideCompanionsConfirmationAlertTime = rideCompanionsConfirmationAlertTime;
        this.rideService = rideService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void notifyRideOwner() {
        LOG.debug("Job starting: notifyRideOwner");
        List<Ride> rides = rideService.findAllByStatusAndDepartureDateBefore(Ride.Status.READY,
            LocalDateTime.now().plusMinutes(rideOwnerConfirmationAlertTime));
        LOG.debug("notifyRideOwner: found {} rides", rides.size());
        for (Ride ride : rides) {
            try {
                sendAlert(ride, ride.getOwner());
                ride.setStatus(Ride.Status.NOTIFIED_OWNER);
                rideService.save(ride);
            } catch (IOException | SlackApiException e) {
                LOG.error("Failed to send owner confirmation ride={}", ride, e);
            }
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void notifyRideCompanions() {
        LOG.debug("Job starting: notifyRideCompanions");
        List<Ride> rides = rideService.findAllByStatusAndDepartureDateBefore(Ride.Status.NOTIFIED_OWNER,
            LocalDateTime.now().plusMinutes(rideCompanionsConfirmationAlertTime));
        LOG.debug("notifyRideCompanions: found {} rides", rides.size());
        for (Ride ride : rides) {
            sendCompanionsAlert(ride);
            ride.setStatus(Ride.Status.NOTIFIED_COMPANIONS);
            rideService.save(ride);
        }
    }

    private ChatPostMessageResponse sendAlert(Ride ride, User user) throws IOException, SlackApiException {
        return Slack.getInstance().methods().chatPostMessage(ChatPostMessageRequest.builder()
            .token(botAccessToken)
            .channel(user.getSlackInfo().getChannelId())
            .attachments(Lists.newArrayList(Attachment.builder()
                .callbackId(CallbackId.RIDE_LIST)
                .pretext("Your ride is about to start")
                .fields(RideView.asFields(ride))
                .actions(RideView.createActionsFor(ride, user))
                .build()))
            .build());
    }

    private void sendCompanionsAlert(Ride ride) {
        Set<User> uniqueCompanions = new HashSet<>(ride.getCompanions());
        for (User user : uniqueCompanions) {
            if (!user.equals(ride.getOwner())) {
                try {
                    sendAlert(ride, user);
                } catch (SlackApiException | IOException e) {
                    LOG.error("Failed to send companion confirmation ride={} companion={}", ride, user, e);
                }
            }
        }
    }
}
