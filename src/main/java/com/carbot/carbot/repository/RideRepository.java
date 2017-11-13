package com.carbot.carbot.repository;

import com.carbot.carbot.model.Ride;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface RideRepository extends MongoRepository<Ride, String> {
    List<Ride> findAllByOwnerId(String ownerId);
    List<Ride> findAllByState(Ride.State state);
    Ride findTopByOwnerIdOrderByCreatedDateDesc(String ownerId);
}
