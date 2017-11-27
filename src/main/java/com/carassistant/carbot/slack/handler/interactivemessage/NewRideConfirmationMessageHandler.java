package com.carassistant.carbot.slack.handler.interactivemessage;

import com.carassistant.model.Ride;
import com.carassistant.service.RideService;
import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.annotation.SlackInteractiveMessageActionHandler;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.handler.ActionValue;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.DeleteLastMessage;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler(callbackId = CallbackId.RIDE_SHARE_CONFIRMATION)
public class NewRideConfirmationMessageHandler {
    private String botAccessToken;
    private RideService rideService;

    @Autowired
    public NewRideConfirmationMessageHandler(@Value("${slack.bot.access.token}") String botAccessToken,
                                             RideService rideService) {
        this.botAccessToken = botAccessToken;
        this.rideService = rideService;
    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.CANCEL)
    public SlackApiResponse onCancel(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getName();
        Ride ride = rideService.findByIdAndStatusIn(rideId, newArrayList(Ride.Status.PENDING_CONFIRMATION));
        rideService.delete(ride);

        return Slack.getInstance().methods()
            .chatDelete(DeleteLastMessage.createRequest(botAccessToken, payload));
    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.OK)
    public SlackApiResponse onOk(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getName();
        Ride ride = rideService.findByIdAndStatusIn(rideId, newArrayList(Ride.Status.PENDING_CONFIRMATION));
        ride.setStatus(Ride.Status.READY);
        ride = rideService.save(ride);

        return Slack.getInstance().methods().chatUpdate(ChatUpdateRequest.builder()
            .token(botAccessToken)
            .channel(payload.getChannel().getId())
            .ts(payload.getOriginalMessage().getTs())
            .text("Shared! Thanks!")
            .attachments(newArrayList())
            .build());
    }
}
