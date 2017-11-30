package com.carassistant.event.ride;

import com.carassistant.model.Ride;

import java.time.LocalDateTime;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class RideDepartureDateChangedEvent extends RideEvent {
    private LocalDateTime prevDepartureDate;

    public RideDepartureDateChangedEvent(Ride ride, LocalDateTime prevDepartureDate) {
        super(ride);
        this.prevDepartureDate = prevDepartureDate;
    }

    public LocalDateTime getPrevDepartureDate() {
        return prevDepartureDate;
    }
}
