package com.carassistant.repository;

import com.carassistant.model.Ride;
import com.carassistant.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface RideRepository extends PagingAndSortingRepository<Ride, String> {
    Ride findOneByIdAndStatusIn(String id, List<Ride.Status> statuses);
    Iterable<Ride> findAllByOwnerIdOrderByCreatedDateDesc(String ownerId);
    Iterable<Ride> findAllByCompanionsContaining(User user);
    Iterable<Ride> findAllByOwnerOrCompanionsContaining(User user, List<User> users);
    Page<Ride> findAllByStatus(Ride.Status status, Pageable pageable);
    Page<Ride> findAllByStatusAndLocation(Ride.Status status, String location, Pageable pageable);
    List<Ride> findAllByStatusAndDepartureDateBefore(Ride.Status status, LocalDateTime date);
    List<Ride> findAllByStatusNotInAndDepartureDateBefore(List<Ride.Status> statuses, LocalDateTime date);
    Ride findOneByOwnerSlackInfoUserIdAndStatus(String ownerSlackId, Ride.Status status);
}
