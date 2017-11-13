package com.carbot.carbot.event;

import com.carbot.carbot.model.Ride;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideSharedEvent extends CarbotEvent {
    private Ride ride;

    public RideSharedEvent(Ride ride) {
        this.ride = ride;
    }

    public Ride getRide() {
        return ride;
    }

    @Override
    public String toString() {
        return "RideSharedEvent{" +
            "ride=" + ride +
            "} " + super.toString();
    }
}
