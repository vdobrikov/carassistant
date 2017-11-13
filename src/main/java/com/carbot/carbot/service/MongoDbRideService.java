package com.carbot.carbot.service;

import com.carbot.carbot.model.Ride;
import com.carbot.carbot.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@Service
public class MongoDbRideService implements RideService {
    private RideRepository rideRepository;

    @Autowired
    public MongoDbRideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Override
    public Optional<Ride> findById(String id) {
        return Optional.ofNullable(rideRepository.findOne(id));
    }

    @Override
    public List<Ride> findAll() {
        return rideRepository.findAll();
    }

    @Override
    public List<Ride> findAllByState(Ride.State state) {
        return rideRepository.findAllByState(state);
    }

    @Override
    public List<Ride> findAllByOwnerId(String ownerId) {
        return rideRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public Optional<Ride> findLastByOwnerId(String ownerId) {
        return Optional.ofNullable(rideRepository.findTopByOwnerIdOrderByCreatedDateDesc(ownerId));
    }

    @Override
    public Ride save(Ride ride) {
        return rideRepository.save(ride);
    }

    @Override
    public void delete(Ride ride) {
        rideRepository.delete(ride);
    }
}
