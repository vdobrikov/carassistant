package com.carassistant.event.ride;

import com.carassistant.model.Ride;

import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideLifecycleEvent extends RideEvent {
    protected Ride.Status oldStatus;
    protected Ride.Status newStatus;

    public RideLifecycleEvent(Ride ride, Ride.Status oldStatus, Ride.Status newStatus) {
        super(ride);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public Optional<Ride.Status> getOldStatus() {
        return Optional.ofNullable(oldStatus);
    }

    public Ride.Status getNewStatus() {
        return newStatus;
    }

    @Override
    public String toString() {
        return "RideLifecycleEvent{" +
            "oldStatus=" + oldStatus +
            ", newStatus=" + newStatus +
            "} " + super.toString();
    }
}
