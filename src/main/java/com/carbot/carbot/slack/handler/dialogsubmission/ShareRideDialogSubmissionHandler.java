package com.carbot.carbot.slack.handler.dialogsubmission;

import com.carbot.carbot.exception.ValueValidationException;
import com.carbot.carbot.model.Ride;
import com.carbot.carbot.service.RideService;
import com.carbot.carbot.slack.annotation.SlackDialogSubmissionHandler;
import com.carbot.carbot.slack.annotation.SlackHandler;
import com.carbot.carbot.slack.handler.ActionValue;
import com.carbot.carbot.slack.handler.CallbackId;
import com.carbot.carbot.slack.model.ActionPayload;
import com.carbot.carbot.slack.model.DialogSubmissionError;
import com.carbot.carbot.slack.model.DialogSubmissionErrorsResponse;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
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

    private String apiBotToken;
    private RideService rideService;

    @Autowired
    public ShareRideDialogSubmissionHandler(@Value("${slack.bot.access.token}") String apiBotToken, RideService rideService) {
        this.apiBotToken = apiBotToken;
        this.rideService = rideService;
    }

    @SlackDialogSubmissionHandler(callbackId = CallbackId.SHARE_RIDE_DIALOG)
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

        LOG.info("onCallbackShareRideDialog: passed validation");

        Ride ride = Ride.builder()
            .setOwnerId(payload.getUser().getId())
            .setDeparturePoint(departurePoint)
            .setDestinationPoint(destinationPoint)
            .setDepartureDate(departureTime)
            .setFreeSeats(freeSeats)
            .setComment(comment)
            .setStatus(Ride.Status.PENDING_CONFIRMATION)
            .build();
        ride = rideService.save(ride);

        Slack.getInstance().methods().chatPostMessage(ChatPostMessageRequest.builder()
            .token(apiBotToken)
            .channel(payload.getChannel().getId())
            .text("How does it look like?")
            .attachments(
                Lists.newArrayList(
                    asAttachment(ride),
                    Attachment.builder()
                        .text("Should I share this ride?")
                        .fallback("This bot requires interactive messages support")
                        .callbackId(CallbackId.NEW_RIDE_CONFIRMATION_PROMPT)
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
            throw new ValueValidationException(String.format("Departure date is in the past: %s", departureDate.format(DateTimeFormatter.ofPattern("MMM, dd HH:mm"))));
        }
        return departureDate;
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
