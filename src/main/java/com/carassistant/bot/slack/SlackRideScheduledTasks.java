package com.carassistant.bot.slack;

import com.carassistant.bot.slack.handler.ActionName;
import com.carassistant.bot.slack.framework.SlackApiClient;
import com.carassistant.bot.slack.message.RideView;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.service.RideService;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Component
public class SlackRideScheduledTasks {
    private static final Logger LOG = LoggerFactory.getLogger(SlackRideScheduledTasks.class);

    private int rideOwnerConfirmationAlertTime;
    private int rideCompanionsConfirmationAlertTime;
    private RideService rideService;
    private SlackApiClient slackApiClient;

    @Autowired
    public SlackRideScheduledTasks(@Value("${carassistant.alert.ride.owner_before_time_minutes}") int rideOwnerConfirmationAlertTime,
                                   @Value("${carassistant.alert.ride.companions_before_time_minutes}") int rideCompanionsConfirmationAlertTime,
                                   RideService rideService,
                                   SlackApiClient slackApiClient) {
        this.rideOwnerConfirmationAlertTime = rideOwnerConfirmationAlertTime;
        this.rideCompanionsConfirmationAlertTime = rideCompanionsConfirmationAlertTime;
        this.rideService = rideService;
        this.slackApiClient = slackApiClient;
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

    private ChatPostMessageResponse sendAlert(Ride ride, User user) throws IOException, SlackApiException {
        LinkedList<Action> actions = RideView.createActionsFor(ride, user);
        if (ride.getOwner().equals(user)) {
            actions.addFirst(Action.builder()
                .type(Action.Type.SELECT)
                .name(ActionName.POSTPONE)
                .text("Postpone For...")
                .options(newArrayList(
                    Option.builder().text("5 min").value("5").build(),
                    Option.builder().text("10 min").value("10").build(),
                    Option.builder().text("15 min").value("15").build(),
                    Option.builder().text("30 min").value("30").build(),
                    Option.builder().text("1 hour").value("60").build()))
                .build());
        }
        return slackApiClient.send(ChatPostMessageRequest.builder()
            .channel(user.getSlackInfo().getChannelId())
            .attachments(newArrayList(Attachment.builder()
                .callbackId(RideView.createCallbackFor(ride))
                .pretext("Your ride is about to start")
                .fields(RideView.asFields(ride))
                .actions(actions)
                .build()))
            .build());
    }
}
