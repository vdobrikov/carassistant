package com.carassistant.event.ride;

import com.carassistant.model.Ride;
import com.carassistant.event.CarAssistantEvent;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideEvent extends CarAssistantEvent {
    protected Ride ride;

    public RideEvent(Ride ride) {
        this.ride = ride;
    }

    public Ride getRide() {
        return ride;
    }

    @Override
    public String toString() {
        return "RideEvent{" +
            "ride=" + ride +
            "} " + super.toString();
    }
}
