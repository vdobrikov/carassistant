package com.carassistant.carbot.slack.handler.interactivemessage;

import com.carassistant.event.ride.RideCompanionEvent;
import com.carassistant.model.Ride;
import com.carassistant.carbot.slack.framework.UserContext;
import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.DeleteLastMessage;
import com.carassistant.model.User;
import com.carassistant.service.RideService;
import com.carassistant.carbot.slack.framework.annotation.SlackInteractiveMessageActionHandler;
import com.carassistant.carbot.slack.handler.ActionValue;
import com.carassistant.carbot.slack.message.RideView;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.model.Attachment;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler(callbackId = CallbackId.RIDE_LIST)
public class RideJoinHandler {
    private static final List<Ride.Status> RIDE_ACTIVE_STATUSES = ImmutableList.of(Ride.Status.READY, Ride.Status.NOTIFIED_OWNER, Ride.Status.NOTIFIED_COMPANIONS);

    private String botAccessToken;
    private RideService rideService;
    private UserContext userContext;
    private ApplicationEventPublisher eventPublisher;

    public RideJoinHandler(@Value("${slack.bot.access.token}") String botAccessToken,
                           RideService rideService,
                           UserContext userContext,
                           ApplicationEventPublisher eventPublisher) {

        this.botAccessToken = botAccessToken;
        this.rideService = rideService;
        this.userContext = userContext;
        this.eventPublisher = eventPublisher;
    }

    @SlackInteractiveMessageActionHandler(actionValue = {ActionValue.JOIN, ActionValue.JOIN_PLUS_1})
    public SlackApiResponse onJoin(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getName();
        Ride ride = rideService.findByIdAndStatusIn(rideId, RIDE_ACTIVE_STATUSES);

        User user = userContext.getUser();
        ride.addCompanion(user);
        rideService.save(ride);
        eventPublisher.publishEvent(new RideCompanionEvent(ride, user, RideCompanionEvent.Action.JOIN));

        return Slack.getInstance().methods()
            .chatUpdate(ChatUpdateRequest.builder()
                .token(botAccessToken)
                .channel(payload.getChannel().getId())
                .ts(payload.getMessageTs())
                .attachments(Lists.newArrayList(Attachment.builder()
                    .callbackId(CallbackId.RIDE_LIST)
                    .pretext(String.format("Joined to <@%s>'s ride", ride.getOwner().getSlackInfo().getUserId()))
                    .fields(RideView.asFields(ride))
                    .actions(RideView.createActionsFor(ride, user))
                    .build()))
                .build());
    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.UNJOIN)
    public SlackApiResponse onUnjoin(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getName();
        Ride ride = rideService.findByIdAndStatusIn(rideId, RIDE_ACTIVE_STATUSES);

        User user = userContext.getUser();
        ride.getCompanions().remove(user);
        rideService.save(ride);
        eventPublisher.publishEvent(new RideCompanionEvent(ride, user, RideCompanionEvent.Action.UNJOIN));

        return postUnjoinedMessage(payload, ride, user);
    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.UNJOIN_ALL)
    public SlackApiResponse onUnjoinAll(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getName();
        Ride ride = rideService.findByIdAndStatusIn(rideId, RIDE_ACTIVE_STATUSES);

        User user = userContext.getUser();
        List<User> companions = ride.getCompanions();
        boolean wasRemoved;
        do {
            wasRemoved = companions.remove(user);
            if (wasRemoved) {
                rideService.save(ride);
                eventPublisher.publishEvent(new RideCompanionEvent(ride, user, RideCompanionEvent.Action.UNJOIN));
            }
        } while (wasRemoved);

        return postUnjoinedMessage(payload, ride, user);
    }

    private SlackApiResponse postUnjoinedMessage(ActionPayload payload, Ride ride, User user) throws IOException, SlackApiException {
        return Slack.getInstance().methods()
            .chatUpdate(ChatUpdateRequest.builder()
                .token(botAccessToken)
                .channel(payload.getChannel().getId())
                .ts(payload.getMessageTs())
                .attachments(Lists.newArrayList(Attachment.builder()
                    .callbackId(CallbackId.RIDE_LIST)
                    .pretext(String.format("Unjoined <@%s>'s ride", ride.getOwner().getSlackInfo().getUserId()))
                    .fields(RideView.asFields(ride))
                    .actions(RideView.createActionsFor(ride, user))
                    .build()))
                .build());
    }

    @SlackInteractiveMessageActionHandler(actionValue = ActionValue.DELETE)
    public SlackApiResponse onDelete(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getName();
        Ride ride = rideService.findByIdAndStatusIn(rideId, RIDE_ACTIVE_STATUSES);

        ride.setStatus(Ride.Status.CANCELLED);
        rideService.save(ride);

        return Slack.getInstance().methods()
            .chatDelete(DeleteLastMessage.createRequest(botAccessToken, payload));
    }
}
