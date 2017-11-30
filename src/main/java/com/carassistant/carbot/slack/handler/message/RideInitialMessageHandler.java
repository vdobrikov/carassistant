package com.carassistant.carbot.slack.handler.message;

import com.carassistant.carbot.slack.framework.SlackApiClient;
import com.carassistant.carbot.slack.framework.UserContext;
import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.annotation.SlackMessageActionHandler;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.handler.ActionName;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.carbot.slack.message.RidesView;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.service.ConfigService;
import com.carassistant.service.RideService;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.dialog.DialogOpenRequest;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.dialog.Dialog;
import com.github.seratch.jslack.api.model.dialog.DialogSubType;
import com.github.seratch.jslack.api.model.dialog.DialogTextElement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.carassistant.carbot.slack.message.CommonRequests.deleteOriginalMessage;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler(callbackId = CallbackId.RIDE_INITIAL_PROMPT)
public class RideInitialMessageHandler {
    private static final List<Ride.Status> RIDE_ACTIVE_STATUSES = ImmutableList.of(Ride.Status.READY, Ride.Status.NOTIFIED_OWNER, Ride.Status.NOTIFIED_COMPANIONS);

    private int pageSize;
    private RideService rideService;
    private ConfigService configService;
    private UserContext userContext;
    private SlackApiClient slackApiClient;

    @Autowired
    public RideInitialMessageHandler(@Value("${slack.pagination.size}") int pageSize,
                                     RideService rideService,
                                     ConfigService configService,
                                     UserContext userContext,
                                     SlackApiClient slackApiClient) {
        this.pageSize = pageSize;
        this.rideService = rideService;
        this.configService = configService;
        this.userContext = userContext;
        this.slackApiClient = slackApiClient;
    }

    @SlackMessageActionHandler(actionName = ActionName.CANCEL)
    public SlackApiResponse onCancel(ActionPayload payload) throws IOException, SlackApiException {
        return slackApiClient.send(deleteOriginalMessage(payload));
    }

    @SlackMessageActionHandler(actionName = ActionName.RIDE_SHARE)
    public SlackApiResponse onRideShare(ActionPayload payload) throws IOException, SlackApiException {
        String departurePoint = null;
        String destinationPoint = null;
        String freeSeats = "3";

        User localUser = userContext.getUser();
        Map<String, String> locations = configService.getLocations();
        departurePoint = locations.get(localUser.getLocation());

        return slackApiClient.send(DialogOpenRequest.builder()
                .triggerId(payload.getTriggerId())
                .dialog(Dialog.builder()
                    .title("Ride Sharing")
                    .callbackId(CallbackId.RIDE_SHARE_DIALOG)
                    .elements(Lists.newArrayList(
                        DialogTextElement.builder()
                            .name("departurePoint")
                            .label("Departure Point")
                            .hint("Your departure point")
                            .placeholder("Bunina, 1")
                            .value(departurePoint)
                            .minLength(1)
                            .maxLength(100)
                            .build(),
                        DialogTextElement.builder()
                            .name("destinationPoint")
                            .label("Destination Point")
                            .hint("Where are you going?")
                            .placeholder("Deribasovskaya, 1")
                            .value(destinationPoint)
                            .minLength(1)
                            .maxLength(100)
                            .build(),
                        DialogTextElement.builder()
                            .name("departureTime")
                            .label("Departure Time")
                            .hint("What time are you leaving?")
                            .placeholder("7pm")
                            .minLength(1)
                            .maxLength(100)
                            .build(),
                        DialogTextElement.builder()
                            .subtype(DialogSubType.NUMBER)
                            .name("freeSeats")
                            .label("Free Seats")
                            .hint("How many friends can join?")
                            .placeholder("3")
                            .value(freeSeats)
                            .minLength(1)
                            .maxLength(3)
                            .build(),
                        DialogTextElement.builder()
                            .name("comment")
                            .label("Comment")
                            .hint("Any additional information")
                            .minLength(1)
                            .maxLength(100)
                            .optional(true)
                            .build()))
                    .submitLabel("Share")
                    .build())
                .build());
    }

    @SlackMessageActionHandler(actionName = ActionName.RIDE_LIST)
    public SlackApiResponse onRideList(ActionPayload payload) throws IOException, SlackApiException {
        User localUser = userContext.getUser();

        Page<Ride> ridesPage = rideService.findAllByStatusAndLocation(Ride.Status.READY, localUser.getLocation(),
            new PageRequest(0, pageSize, Sort.Direction.DESC, "createdDate"));


        String text;
        List<Attachment> attachments;
        if (ridesPage.hasContent()) {
            text = null;
            attachments = RidesView.createAttachmentsFor(CallbackId.RIDE_LIST, localUser, ridesPage);
        } else {
            text = "No shared rides were found this time. Please try again later.";
            attachments = null;
        }

        return slackApiClient.send(ChatPostMessageRequest.builder()
                .channel(payload.getChannel().getId())
                .text(text)
                .attachments(attachments)
                .build());
    }

    @SlackMessageActionHandler(actionName = ActionName.RIDE_LIST_MY)
    public SlackApiResponse onRideListMy(ActionPayload payload) throws IOException, SlackApiException {
        User localUser = userContext.getUser();

        Iterable<Ride> rawRides = rideService.findAllRelatedToUser(localUser);
        List<Ride> rides = StreamSupport.stream(rawRides.spliterator(), false)
            .filter(ride -> RIDE_ACTIVE_STATUSES.contains(ride.getStatus()))
            .limit(20) // Max num of attachments in Slack
            .collect(Collectors.toList());

        String text;
        List<Attachment> attachments;

        if (!rides.isEmpty()) {
            text = null;
            attachments = RidesView.createAttachmentsFor(CallbackId.RIDE_LIST, localUser, rides);
        } else {
            text = "You have no related rides";
            attachments = null;
        }

        return slackApiClient.send(ChatPostMessageRequest.builder()
                .channel(payload.getChannel().getId())
                .text(text)
                .attachments(attachments)
                .build());
    }
}
