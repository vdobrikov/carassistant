package com.carassistant.event.ride;

import com.carassistant.model.Ride;
import com.carassistant.model.User;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideCompanionEvent extends RideEvent {
    private User companion;
    private Action action;

    public RideCompanionEvent(Ride ride, User companion, Action action) {
        super(ride);
        this.companion = companion;
        this.action = action;
    }

    public User getCompanion() {
        return companion;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "RideJoinedEvent{" +
            "companion=" + companion +
            "} " + super.toString();
    }

    public enum Action {
        JOIN,
        UNJOIN
    }
}
