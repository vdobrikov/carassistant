package com.carassistant.bot.slack.message;

import com.carassistant.bot.slack.handler.ActionName;
import com.carassistant.bot.slack.handler.CallbackId;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Confirmation;
import com.github.seratch.jslack.api.model.Field;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideView {
    private static final List<Ride.Status> RIDE_ACTIVE_STATUSES = ImmutableList.of(Ride.Status.READY, Ride.Status.NOTIFIED_OWNER, Ride.Status.NOTIFIED_COMPANIONS);

    public static Attachment.AttachmentBuilder asAttachmentBuilder(Ride ride) {
        return Attachment.builder()
            .fields(RideView.asFields(ride));
    }

    public static String createCallbackFor(Ride ride) {
        return String.format(CallbackId.RIDE_ACTIONS.replaceAll("\\{.+?\\}", "%s"), ride.getId());
    }

    public static List<Field> asFields(Ride ride) {
        List<String> companions = ride.getCompanions().stream()
            .map(companion -> companion.getSlackInfo().getUserId())
            .map(companionSlackId -> String.format("<@%s>", companionSlackId))
            .collect(Collectors.toList());
        return newArrayList(
            Field.builder().valueShortEnough(true).title("Departure Point").value(ride.getDeparturePoint()).build(),
            Field.builder().valueShortEnough(true).title("Destination Point").value(ride.getDestinationPoint()).build(),
            Field.builder().valueShortEnough(true).title("Departure Time").value(ride.getDepartureDate().format(DateTimeFormatter.ofPattern("MMM, dd HH:mm"))).build(),
            Field.builder().valueShortEnough(true).title("Free Seats").value(String.valueOf(ride.getFreeSeats())).build(),
            Field.builder().valueShortEnough(true).title("Comment").value(ride.getComment()).build(),
            Field.builder().valueShortEnough(true).title("Companions").value(Joiner.on(", ").join(companions)).build());
    }

    public static LinkedList<Action> createActionsFor(Ride ride, User user) {
        if (!RIDE_ACTIVE_STATUSES.contains(ride.getStatus())) {
            return newLinkedList();
        }
        Set<AvailableActions> availableActions = getAvailableActionsFor(ride, user);
        LinkedList<Action> actions = new LinkedList<>();
        if (availableActions.contains(AvailableActions.JOIN)) {
            actions.add(Action.builder()
                .type(Action.Type.BUTTON)
                .text("Join")
                .name(ActionName.JOIN)
                .build());
        }
        if (availableActions.contains(AvailableActions.JOIN_PLUS_1)) {
            actions.add(Action.builder()
                .type(Action.Type.BUTTON)
                .text("Join (+1)")
                .name(ActionName.JOIN_PLUS_1)
                .build());
        }
        if (availableActions.contains(AvailableActions.UNJOIN)) {
            actions.add(Action.builder()
                .type(Action.Type.BUTTON)
                .text("Unjoin")
                .name(ActionName.UNJOIN)
                .build());
        }
        if (availableActions.contains(AvailableActions.UNJOIN_ALL)) {
            actions.add(Action.builder()
                .type(Action.Type.BUTTON)
                .text("Unjoin All")
                .name(ActionName.UNJOIN_ALL)
                .build());
        }
        if (availableActions.contains(AvailableActions.DELETE)) {
            actions.add(Action.builder()
                .type(Action.Type.BUTTON)
                .text("Delete")
                .name(ActionName.DELETE)
                .style("danger")
                .confirmation(
                    Confirmation.builder()
                        .title("Delete ride share")
                        .text("Are you sure?")
                        .ok_text("Delete")
                        .dismiss_text("Cancel")
                        .build())
                .build());
        }
        return actions;
    }

    private static Set<AvailableActions> getAvailableActionsFor(Ride ride, User user) {
        Set<AvailableActions> actions = new HashSet<>();
        if (ride.getCompanions().contains(user)) {
            actions.add(AvailableActions.UNJOIN);
            if (Collections.frequency(ride.getCompanions(), user) > 1) {
                actions.add(AvailableActions.UNJOIN_ALL);
            }
            if (ride.getFreeSeats() > 0) {
                actions.add(AvailableActions.JOIN_PLUS_1);
            }
        } else if (ride.getFreeSeats() > 0) {
            actions.add(AvailableActions.JOIN);
        }
        if (user.equals(ride.getOwner())) {
            actions.add(AvailableActions.DELETE);
        }
        return actions;
    }

    private enum AvailableActions {
        JOIN,
        JOIN_PLUS_1,
        UNJOIN,
        UNJOIN_ALL,
        DELETE
    }
}
