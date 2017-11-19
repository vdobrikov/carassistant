package com.carbot.carbot.slack.handler.interactivemessage;

import com.carbot.carbot.event.RideSharedEvent;
import com.carbot.carbot.exception.CarBotException;
import com.carbot.carbot.model.Ride;
import com.carbot.carbot.service.RideService;
import com.carbot.carbot.slack.annotation.SlackHandler;
import com.carbot.carbot.slack.annotation.SlackInteractiveMessageActionHandler;
import com.carbot.carbot.slack.handler.ActionValue;
import com.carbot.carbot.slack.handler.CallbackId;
import com.carbot.carbot.slack.model.ActionPayload;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatDeleteRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatDeleteResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatUpdateResponse;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler(callbackId = CallbackId.NEW_RIDE_CONFIRMATION_PROMPT)
public class NewRideConfirmationMessageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(NewRideConfirmationMessageHandler.class);

    private String apiBotToken;
    private RideService rideService;
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public NewRideConfirmationMessageHandler(@Value("${slack.bot.access.token}") String apiBotToken,
                                             RideService rideService,
                                             ApplicationEventPublisher eventPublisher) {
        this.apiBotToken = apiBotToken;
        this.rideService = rideService;
        this.eventPublisher = eventPublisher;
    }


    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.CANCEL)
    public void onCancel(ActionPayload payload) throws IOException, SlackApiException {
        Ride ride = rideService.findOneByOwnerIdAndStatus(payload.getUser().getId(), Ride.Status.PENDING_CONFIRMATION);
        rideService.delete(ride);

        ChatDeleteResponse chatDeleteResponse = Slack.getInstance().methods().chatDelete(ChatDeleteRequest.builder()
            .token(apiBotToken)
            .channel(payload.getChannel().getId())
            .ts(payload.getOriginalMessage().getTs())
            .build());
        LOG.info("chatDeleteResponse={}", chatDeleteResponse);
    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.OK)
    public void onOk(ActionPayload payload) throws IOException, SlackApiException {
        String tmpRideId = payload.getActions().get(0).getName();
        Ride ride = rideService.findById(tmpRideId).orElseThrow(() -> new CarBotException("Temporary ride wasn't found. id=" + tmpRideId));
        ride.setStatus(Ride.Status.READY);
        rideService.save(ride);
        eventPublisher.publishEvent(new RideSharedEvent(ride));

        ChatUpdateResponse chatUpdateResponse = Slack.getInstance().methods().chatUpdate(ChatUpdateRequest.builder()
            .token(apiBotToken)
            .channel(payload.getChannel().getId())
            .ts(payload.getOriginalMessage().getTs())
            .text("Shared! Thanks!")
            .attachments(Lists.newArrayList(
                asAttachment(ride)))
            .build());
        LOG.info("chatUpdateResponse={}", chatUpdateResponse);
    }

    private Attachment asAttachment(Ride ride) {
        return Attachment.builder()
            .fields(asFields(ride))
            .build();
    }

    private String asText(Ride ride) {
        return String.format("From: *%s* to *%s* at *%s* with *%s* seats // *%s*",
            ride.getDeparturePoint(), ride.getDestinationPoint(),
            ride.getDepartureDate().format(DateTimeFormatter.ofPattern("MMM, dd HH:mm")), ride.getFreeSeats(), ride.getComment());
    }

    private List<Field> asFields(Ride ride) {
        return Lists.newArrayList(
            Field.builder().valueShortEnough(true).title("Departure Point").value(ride.getDeparturePoint()).build(),
            Field.builder().valueShortEnough(true).title("Destination Point").value(ride.getDestinationPoint()).build(),
            Field.builder().valueShortEnough(true).title("Departure Time").value(ride.getDepartureDate().format(DateTimeFormatter.ofPattern("MMM, dd HH:mm"))).build(),
            Field.builder().valueShortEnough(true).title("Free Seats").value(String.valueOf(ride.getFreeSeats())).build(),
            Field.builder().valueShortEnough(true).title("Comment").value(ride.getComment()).build());
    }
}
