package com.carassistant.carbot.slack.handler.message;

import com.carassistant.carbot.slack.framework.SlackApiClient;
import com.carassistant.carbot.slack.framework.UserContext;
import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.annotation.SlackMessageActionHandler;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.handler.ActionName;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.CommonRequests;
import com.carassistant.carbot.slack.message.RideView;
import com.carassistant.event.ride.RideCompanionEvent;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.service.RideService;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.model.Attachment;
import com.google.common.collect.ImmutableList;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler(callbackId = CallbackId.RIDE_LIST)
public class RideActionsHandler {
    private static final List<Ride.Status> RIDE_ACTIVE_STATUSES = ImmutableList.of(
        Ride.Status.READY, Ride.Status.NOTIFIED_OWNER, Ride.Status.NOTIFIED_COMPANIONS);

    private RideService rideService;
    private UserContext userContext;
    private ApplicationEventPublisher eventPublisher;
    private SlackApiClient slackApiClient;

    public RideActionsHandler(RideService rideService,
                              UserContext userContext,
                              ApplicationEventPublisher eventPublisher,
                              SlackApiClient slackApiClient) {

        this.rideService = rideService;
        this.userContext = userContext;
        this.eventPublisher = eventPublisher;
        this.slackApiClient = slackApiClient;
    }

    @SlackMessageActionHandler(actionName = {ActionName.JOIN, ActionName.JOIN_PLUS_1})
    public SlackApiResponse onJoin(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getValue();
        Optional<Ride> rideOptional = rideService.findByIdAndStatusIn(rideId, RIDE_ACTIVE_STATUSES);
        if (!rideOptional.isPresent()) {
            return slackApiClient.send(CommonRequests.updateOriginalMessage(payload, "Ride doesn't exist"));
        }
        Ride ride = rideOptional.get();

        User user = userContext.getUser();
        ride.addCompanion(user);
        rideService.save(ride);
        eventPublisher.publishEvent(new RideCompanionEvent(ride, user, RideCompanionEvent.Action.JOIN));

        return slackApiClient.send(ChatUpdateRequest.builder()
                .channel(payload.getChannel().getId())
                .ts(payload.getMessageTs())
                .attachments(newArrayList(Attachment.builder()
                    .callbackId(CallbackId.RIDE_LIST)
                    .pretext(String.format("Joined to <@%s>'s ride", ride.getOwner().getSlackInfo().getUserId()))
                    .fields(RideView.asFields(ride))
                    .actions(RideView.createActionsFor(ride, user))
                    .build()))
                .build());
    }

    @SlackMessageActionHandler(actionName = ActionName.UNJOIN)
    public SlackApiResponse onUnjoin(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getValue();
        Optional<Ride> rideOptional = rideService.findByIdAndStatusIn(rideId, RIDE_ACTIVE_STATUSES);
        if (!rideOptional.isPresent()) {
            return slackApiClient.send(CommonRequests.updateOriginalMessage(payload, "Ride doesn't exist"));
        }

        Ride ride = rideOptional.get();
        User user = userContext.getUser();
        ride.getCompanions().remove(user);
        rideService.save(ride);
        eventPublisher.publishEvent(new RideCompanionEvent(ride, user, RideCompanionEvent.Action.UNJOIN));

        return postUnjoinedMessage(payload, ride, user);
    }

    @SlackMessageActionHandler(actionName = ActionName.UNJOIN_ALL)
    public SlackApiResponse onUnjoinAll(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getValue();
        Optional<Ride> rideOptional = rideService.findByIdAndStatusIn(rideId, RIDE_ACTIVE_STATUSES);
        if (!rideOptional.isPresent()) {
            return slackApiClient.send(CommonRequests.updateOriginalMessage(payload, "Ride doesn't exist"));
        }

        Ride ride = rideOptional.get();
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
        return slackApiClient.send(ChatUpdateRequest.builder()
                .channel(payload.getChannel().getId())
                .ts(payload.getMessageTs())
                .attachments(newArrayList(Attachment.builder()
                    .callbackId(CallbackId.RIDE_LIST)
                    .pretext(String.format("Unjoined <@%s>'s ride", ride.getOwner().getSlackInfo().getUserId()))
                    .fields(RideView.asFields(ride))
                    .actions(RideView.createActionsFor(ride, user))
                    .build()))
                .build());
    }

    @SlackMessageActionHandler(actionName = ActionName.SNOOZE)
    public SlackApiResponse onSnooze(ActionPayload payload) throws IOException, SlackApiException {

        String snoozeValue = payload.getFirstAction().getFirstSelectedOption().getValue();
        AntPathMatcher pathMatcher = new AntPathMatcher("_");

        // 5a1dc0ebf4026e2ba3bf603b_minutes_30
        Map<String, String> attributesMap = pathMatcher.extractUriTemplateVariables("{id}_minutes_{value}", snoozeValue);

        Optional<Ride> rideOptional = rideService.findByIdAndStatusIn(attributesMap.get("id"), RIDE_ACTIVE_STATUSES);
        if (!rideOptional.isPresent()) {
            return slackApiClient.send(CommonRequests.updateOriginalMessage(payload, "Ride doesn't exist"));
        }

        Ride ride = rideOptional.get();
        ride.setDepartureDate(ride.getDepartureDate().plusMinutes(Long.parseLong(attributesMap.get("value"))));
        ride.setStatus(Ride.Status.READY);
        rideService.save(ride);

        return slackApiClient.send(ChatUpdateRequest.builder()
            .channel(payload.getChannel().getId())
            .ts(payload.getMessageTs())
            .text("Snoozed!")
            .attachments(newArrayList(Attachment.builder()
                .callbackId(CallbackId.RIDE_LIST)
                .fields(RideView.asFields(ride))
                .actions(RideView.createActionsFor(ride, userContext.getUser()))
                .build()))
            .build());
    }

    @SlackMessageActionHandler(actionName = ActionName.DELETE)
    public SlackApiResponse onDelete(ActionPayload payload) throws IOException, SlackApiException {
        String rideId = payload.getActions().get(0).getValue();
        Optional<Ride> rideOptional = rideService.findByIdAndStatusIn(rideId, RIDE_ACTIVE_STATUSES);
        if (!rideOptional.isPresent()) {
            return slackApiClient.send(CommonRequests.deleteOriginalMessage(payload));
        }

        Ride ride = rideOptional.get();
        ride.setStatus(Ride.Status.CANCELLED);
        rideService.save(ride);

        return slackApiClient.send(CommonRequests.deleteOriginalMessage(payload));
    }
}
