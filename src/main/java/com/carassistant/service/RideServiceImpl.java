package com.carassistant.service;

import com.carassistant.model.Ride;
import com.carassistant.repository.RideRepository;
import com.carassistant.event.ride.RideLifecycleEvent;
import com.carassistant.exception.RideNotFoundException;
import com.carassistant.model.User;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@Service
public class RideServiceImpl implements RideService {
    private static final List<Ride.Status> RIDE_ACTIVE_STATUSES = ImmutableList.of(Ride.Status.READY, Ride.Status.NOTIFIED_OWNER, Ride.Status.NOTIFIED_COMPANIONS);

    private RideRepository rideRepository;
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public RideServiceImpl(RideRepository rideRepository, ApplicationEventPublisher eventPublisher) {
        this.rideRepository = rideRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Ride findById(String id) {
        return Optional.ofNullable(rideRepository.findOne(id))
            .orElseThrow(() -> new RideNotFoundException("id=" + id));
    }

    @Override
    public Ride findByIdAndStatusIn(String id, List<Ride.Status> statuses) throws RideNotFoundException {
        return Optional.ofNullable(rideRepository.findOneByIdAndStatusIn(id, statuses))
            .orElseThrow(() -> new RideNotFoundException(String.format("id=%s statuses=%s", id, statuses)));
    }

    @Override
    public Iterable<Ride> findAll() {
        return rideRepository.findAll();
    }

    @Override
    public Iterable<Ride> findAllByOwnerId(String ownerId) {
        return rideRepository.findAllByOwnerIdOrderByCreatedDateDesc(ownerId);
    }

    @Override
    public Iterable<Ride> findAllRelatedToUser(User user) {
        return rideRepository.findAllByOwnerOrCompanionsContaining(user, Lists.newArrayList(user));
    }

    @Override
    public Page<Ride> findAllByStatusAndLocation(Ride.Status status, String location, Pageable pageable) {
        return rideRepository.findAllByStatusAndLocation(status, location, pageable);
    }

    @Override
    public List<Ride> findAllByStatusAndDepartureDateBefore(Ride.Status status, LocalDateTime date) {
        return rideRepository.findAllByStatusAndDepartureDateBefore(status, date);
    }

    @Override
    public List<Ride> findAllByStatusNotInAndDepartureDateBefore(List<Ride.Status> statuses, LocalDateTime date) {
        return rideRepository.findAllByStatusNotInAndDepartureDateBefore(statuses, date);
    }

    @Override
    public Ride save(Ride ride) {
        Ride rideBefore = null;
        if (ride.getId() != null) {
            rideBefore = rideRepository.findOne(ride.getId());
        }
        ride = rideRepository.save(ride);

        if (rideBefore == null || ride.getStatus() != rideBefore.getStatus()) {
            if (rideBefore == null) {
                eventPublisher.publishEvent(new RideLifecycleEvent(ride, null, ride.getStatus()));
            } else {
                eventPublisher.publishEvent(new RideLifecycleEvent(ride, rideBefore.getStatus(), ride.getStatus()));
            }
        }
        return ride;
    }

    @Override
    public void delete(Ride ride) {
        rideRepository.delete(ride);
        eventPublisher.publishEvent(new RideLifecycleEvent(ride, ride.getStatus(), Ride.Status.DELETED_COMPLETELY));
    }

    @Override
    public void delete(String id) {
        Ride ride = rideRepository.findOne(id);
        rideRepository.delete(id);
        Optional.ofNullable(ride).ifPresent(deletedRide ->
            eventPublisher.publishEvent(new RideLifecycleEvent(ride, ride.getStatus(), Ride.Status.DELETED_COMPLETELY)));
    }
}
