package com.carassistant.carbot.slack.message;

import com.carassistant.model.Ride;
import com.carassistant.model.User;
import com.carassistant.carbot.slack.handler.ActionValue;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Confirmation;
import com.github.seratch.jslack.api.model.Field;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideView {
    public static Attachment.AttachmentBuilder asAttachmentBuilder(Ride ride) {
        return Attachment.builder()
            .fields(RideView.asFields(ride));
    }

    public static List<Field> asFields(Ride ride) {
        List<String> companions = ride.getCompanions().stream()
            .map(companion -> companion.getSlackInfo().getUserId())
            .map(companionSlackId -> String.format("<@%s>", companionSlackId))
            .collect(Collectors.toList());
        return Lists.newArrayList(
            Field.builder().valueShortEnough(true).title("Departure Point").value(ride.getDeparturePoint()).build(),
            Field.builder().valueShortEnough(true).title("Destination Point").value(ride.getDestinationPoint()).build(),
            Field.builder().valueShortEnough(true).title("Departure Time").value(ride.getDepartureDate().format(DateTimeFormatter.ofPattern("MMM, dd HH:mm"))).build(),
            Field.builder().valueShortEnough(true).title("Free Seats").value(String.valueOf(ride.getFreeSeats())).build(),
            Field.builder().valueShortEnough(true).title("Comment").value(ride.getComment()).build(),
            Field.builder().valueShortEnough(true).title("Companions").value(Joiner.on(", ").join(companions)).build());
    }

    public static List<Action> createActionsFor(Ride ride, User user) {
        Set<AvailableActions> availableActions = getAvailableActionsFor(ride, user);
        List<Action> actions = new ArrayList<>();
        if (availableActions.contains(AvailableActions.JOIN)) {
            actions.add(createActionBuilder(ride.getId())
                .text("Join")
                .value(ActionValue.JOIN)
                .build());
        }
        if (availableActions.contains(AvailableActions.JOIN_PLUS_1)) {
            actions.add(createActionBuilder(ride.getId())
                .text("Join (+1)")
                .value(ActionValue.JOIN_PLUS_1)
                .build());
        }
        if (availableActions.contains(AvailableActions.UNJOIN)) {
            actions.add(createActionBuilder(ride.getId())
                .text("Unjoin")
                .value(ActionValue.UNJOIN)
                .build());
        }
        if (availableActions.contains(AvailableActions.UNJOIN_ALL)) {
            actions.add(createActionBuilder(ride.getId())
                .text("Unjoin All")
                .value(ActionValue.UNJOIN_ALL)
                .build());
        }
        if (availableActions.contains(AvailableActions.DELETE)) {
            actions.add(createActionBuilder(ride.getId())
                .text("Delete")
                .value(ActionValue.DELETE)
                .style("danger")
                .confirmation(
                    Confirmation.builder()
                        .title("Delete ride share")
                        .text("Are you sure to delete your ride share?")
                        .ok_text("Delete")
                        .dismiss_text("Cancel")
                        .build())
                .build());
        }
        return actions;
    }

    private static Action.ActionBuilder createActionBuilder(String name) {
        return Action.builder()
            .name(name)
            .type(Action.Type.BUTTON);
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
