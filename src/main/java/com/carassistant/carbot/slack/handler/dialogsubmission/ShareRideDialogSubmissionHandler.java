package com.carassistant.carbot.slack.handler.dialogsubmission;

import com.carassistant.model.Ride;
import com.carassistant.carbot.slack.framework.UserContext;
import com.carassistant.carbot.slack.framework.annotation.SlackHandler;
import com.carassistant.carbot.slack.framework.model.ActionPayload;
import com.carassistant.carbot.slack.framework.model.DialogSubmissionErrorsResponse;
import com.carassistant.carbot.slack.handler.CallbackId;
import com.carassistant.exception.ValueValidationException;
import com.carassistant.model.User;
import com.carassistant.service.RideService;
import com.carassistant.carbot.slack.framework.annotation.SlackDialogSubmissionHandler;
import com.carassistant.carbot.slack.framework.model.DialogSubmissionError;
import com.carassistant.carbot.slack.handler.ActionValue;
import com.carassistant.carbot.slack.message.RideView;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.model.Action;
import com.google.common.collect.Lists;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@SlackHandler
public class ShareRideDialogSubmissionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ShareRideDialogSubmissionHandler.class);

    private String botAccessToken;
    private String datetimeFormat;
    private RideService rideService;
    private UserContext userContext;

    @Autowired
    public ShareRideDialogSubmissionHandler(@Value("${slack.bot.access.token}") String botAccessToken,
                                            @Value("${default.datetime.format}") String datetimeFormat,
                                            RideService rideService,
                                            UserContext userContext) {
        this.botAccessToken = botAccessToken;
        this.datetimeFormat = datetimeFormat;
        this.rideService = rideService;
        this.userContext = userContext;
    }

    @SlackDialogSubmissionHandler(callbackId = CallbackId.RIDE_SHARE_DIALOG)
    public DialogSubmissionErrorsResponse onSubmission(ActionPayload payload) throws IOException, SlackApiException {
        List<DialogSubmissionError> errors = new ArrayList<>();

        String departurePoint = payload.getSubmission().get("departurePoint");
        String destinationPoint = payload.getSubmission().get("destinationPoint");
        String rawDepartureTime = payload.getSubmission().get("departureTime");
        String rawFreeSeats = payload.getSubmission().get("freeSeats");
        String comment = payload.getSubmission().get("comment");

        LocalDateTime departureTime = null;
        try {
            departureTime = parseDateTime(rawDepartureTime);
        } catch (ValueValidationException e) {
            errors.add(new DialogSubmissionError("departureTime", e.getMessage()));
        } catch (Exception e) {
            errors.add(new DialogSubmissionError("departureTime", "Failed to parse date"));
        }

        int freeSeats = 0;
        try {
            freeSeats = Integer.parseInt(rawFreeSeats);
        } catch (Exception e) {
            errors.add(new DialogSubmissionError("freeSeats", "Failed to parse free seats amount"));
        }

        if (!errors.isEmpty()) {
            return new DialogSubmissionErrorsResponse(errors);
        }

        LOG.info("Passed validation");

        User localUser = userContext.getUser();
        Ride ride = new Ride();
        ride.setLocation(localUser.getLocation());
        ride.setOwner(localUser);
        ride.setDeparturePoint(departurePoint);
        ride.setDestinationPoint(destinationPoint);
        ride.setDepartureDate(departureTime);
        ride.setFreeSeats(freeSeats);
        ride.setComment(comment);
        ride.setStatus(Ride.Status.PENDING_CONFIRMATION);
        ride = rideService.save(ride);

        Slack.getInstance().methods()
            .chatPostMessage(ChatPostMessageRequest.builder()
                .token(botAccessToken)
                .channel(payload.getChannel().getId())
                .text("How does it look like?")
                .attachments(
                    Lists.newArrayList(
                        RideView.asAttachmentBuilder(ride)
                            .callbackId(CallbackId.RIDE_SHARE_CONFIRMATION)
                            .actions(
                                Lists.newArrayList(
                                    Action.builder().name(ride.getId()).text("Confirm").type(Action.Type.BUTTON).value(ActionValue.OK).style("primary").build(),
                                    Action.builder().name(ride.getId()).text("Cancel").type(Action.Type.BUTTON).value(ActionValue.CANCEL).build()))
                            .build()))
                .build());
        return null;
    }

    private LocalDateTime parseDateTime(String departureTime) {
        LocalDateTime departureDate;
        Parser timeParser = new Parser();
        List<DateGroup> dateGroups = timeParser.parse(departureTime, new Date());
        Assert.isTrue(dateGroups.size() == 1, "dateGroups.size should be 1");
        List<Date> dates = dateGroups.get(0).getDates();
        Assert.isTrue(dates.size() == 1, "dates.size should be 1");
        Date date = dates.get(0);
        departureDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        if (departureDate.isBefore(LocalDateTime.now())) {
            throw new ValueValidationException(String.format("Departure date is in the past: %s", departureDate.format(DateTimeFormatter.ofPattern(datetimeFormat))));
        }
        return departureDate;
    }
}
