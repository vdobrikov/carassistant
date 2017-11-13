package com.carbot.carbot.event;

import com.carbot.carbot.model.Ride;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideCancelledEvent extends CarbotEvent {
    private Ride ride;

    public RideCancelledEvent(Ride ride) {
        this.ride = ride;
    }

    public Ride getRide() {
        return ride;
    }

    @Override
    public String toString() {
        return "RideCancelledEvent{" +
            "ride=" + ride +
            "} " + super.toString();
    }
}
