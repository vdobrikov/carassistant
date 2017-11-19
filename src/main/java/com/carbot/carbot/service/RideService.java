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
    List<Ride> findAllByStatus(Ride.Status status);
    List<Ride> findAllByOwnerId(String ownerId);
    Ride findOneByOwnerIdAndStatus(String ownerId, Ride.Status status);
    Optional<Ride> findLastByOwnerId(String ownerId);
    Ride save(Ride ride);
    void delete(Ride ride);
}
