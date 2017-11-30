package com.carassistant.service;

import com.carassistant.exception.RideNotFoundException;
import com.carassistant.model.Ride;
import com.carassistant.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface RideService {
    Optional<Ride> findById(String id) throws RideNotFoundException;
    Optional<Ride> findByIdAndStatusIn(String id, List<Ride.Status> statuses);
    Iterable<Ride> findAll();
    Iterable<Ride> findAllByOwnerId(String ownerId);
    Iterable<Ride> findAllRelatedToUser(User user);
    Page<Ride> findAllByStatusAndLocation(Ride.Status status, String location, Pageable pageable);
    List<Ride> findAllByStatusAndDepartureDateBefore(Ride.Status status, LocalDateTime date);
    List<Ride> findAllByStatusNotInAndDepartureDateBefore(List<Ride.Status> statuses, LocalDateTime date);
    Ride save(Ride ride);
    void delete(Ride ride);
    void delete(String id);
}
