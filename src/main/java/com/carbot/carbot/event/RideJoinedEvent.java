package com.carbot.carbot.event;

import com.carbot.carbot.model.Ride;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideJoinedEvent extends CarbotEvent {
    private Ride ride;
    private String joinedUserId;

    public RideJoinedEvent(Ride ride, String joinedUserId) {
        this.ride = ride;
        this.joinedUserId = joinedUserId;
    }

    public Ride getRide() {
        return ride;
    }

    public String getJoinedUserId() {
        return joinedUserId;
    }

    @Override
    public String toString() {
        return "RideJoinedEvent{" +
            "ride=" + ride +
            ", joinedUserId='" + joinedUserId + '\'' +
            "} " + super.toString();
    }
}
