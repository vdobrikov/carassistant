package com.carassistant.carbot.slack;

import com.carassistant.carbot.slack.framework.SlackApiClient;
import com.carassistant.carbot.slack.handler.ActionName;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.RideView;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.service.RideService;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Option;
import com.google.common.collect.ImmutableList;
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
    public SlackRideScheduledTasks(@Value("${ride.confirmation.owner.alert.time.minutes}") int rideOwnerConfirmationAlertTime,
                                   @Value("${ride.confirmation.companions.alert.time.minutes}") int rideCompanionsConfirmationAlertTime,
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
                .name(ActionName.SNOOZE)
                .text("Snooze For...")
                .options(newArrayList(
                    Option.builder().text("5 min").value(ride.getId() + "_minutes_5").build(),
                    Option.builder().text("10 min").value(ride.getId() + "_minutes_10").build(),
                    Option.builder().text("15 min").value(ride.getId() + "_minutes_15").build(),
                    Option.builder().text("30 min").value(ride.getId() + "_minutes_30").build(),
                    Option.builder().text("1 hour").value(ride.getId() + "_minutes_60").build()))
                .build());
        }
        return slackApiClient.send(ChatPostMessageRequest.builder()
            .channel(user.getSlackInfo().getChannelId())
            .attachments(newArrayList(Attachment.builder()
                .callbackId(CallbackId.RIDE_LIST)
                .pretext("Your ride is about to start")
                .fields(RideView.asFields(ride))
                .actions(actions)
                .build()))
            .build());
    }
}
