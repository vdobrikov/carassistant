package com.carassistant.carbot.slack.handler.message;

import com.carassistant.carbot.slack.framework.SlackApiClient;
import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.annotation.SlackMessageActionHandler;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.handler.ActionName;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.CommonRequests;
import com.carassistant.model.Ride;
import com.carassistant.service.RideService;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler(callbackId = CallbackId.RIDE_SHARE_CONFIRMATION)
public class NewRideConfirmationMessageHandler {
    private RideService rideService;
    private SlackApiClient slackApiClient;

    @Autowired
    public NewRideConfirmationMessageHandler(RideService rideService, SlackApiClient slackApiClient) {
        this.rideService = rideService;
        this.slackApiClient = slackApiClient;
    }

    @SlackMessageActionHandler(actionName = ActionName.CANCEL)
    public SlackApiResponse onCancel(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getValue();
        Optional<Ride> rideOptional = rideService.findByIdAndStatusIn(rideId, newArrayList(Ride.Status.PENDING_CONFIRMATION));
        if (!rideOptional.isPresent()) {
            return slackApiClient.send(CommonRequests.deleteOriginalMessage(payload));
        }

        Ride ride = rideOptional.get();
        rideService.delete(ride);

        return slackApiClient.send(CommonRequests.deleteOriginalMessage(payload));
    }

    @SlackMessageActionHandler(actionName = ActionName.OK)
    public SlackApiResponse onOk(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getValue();
        Optional<Ride> rideOptional = rideService.findByIdAndStatusIn(rideId, newArrayList(Ride.Status.PENDING_CONFIRMATION));
        if (!rideOptional.isPresent()) {
            return slackApiClient.send(CommonRequests.updateOriginalMessage(payload, "Ride doesn't exist"));
        }

        Ride ride = rideOptional.get();
        ride.setStatus(Ride.Status.READY);
        rideService.save(ride);

        return slackApiClient.send(CommonRequests.updateOriginalMessage(payload, "Shared! Thanks!"));
    }
}
