package com.carassistant.job;

import com.carassistant.model.Ride;
import com.carassistant.service.RideService;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Component
public class RideScheduledTasks {
    private static final Logger LOG = LoggerFactory.getLogger(RideScheduledTasks.class);

    private static final List<Ride.Status> INACTIVE_STATUSES = ImmutableList.of(Ride.Status.CANCELLED, Ride.Status.STALE, Ride.Status.DELETED);

    private RideService rideService;

    @Autowired
    public RideScheduledTasks(RideService rideService) {
        this.rideService = rideService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void markStaleRides() {
        LOG.debug("Job starting: markStaleRides");
        List<Ride> rides = rideService.findAllByStatusNotInAndDepartureDateBefore(INACTIVE_STATUSES, LocalDateTime.now().minusMinutes(1));
        LOG.debug("markStaleRides: found {} rides", rides.size());
        rides.forEach(ride -> {
            ride.setStatus(Ride.Status.STALE);
            rideService.save(ride);
        });
    }
}
