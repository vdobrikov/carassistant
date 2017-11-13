package com.carbot.carbot.service;

import com.carbot.carbot.model.Ride;

import java.util.List;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface RideService {
    Optional<Ride> findById(String id);
    List<Ride> findAll();
    List<Ride> findAllByState(Ride.State state);
    List<Ride> findAllByOwnerId(String ownerId);
    Optional<Ride> findLastByOwnerId(String ownerId);
    Ride save(Ride ride);
    void delete(Ride ride);
}
