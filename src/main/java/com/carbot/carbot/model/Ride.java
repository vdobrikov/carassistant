package com.carbot.carbot.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@Document(collection = "rides")
public class Ride {
    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    private String ownerId;
    private String departurePoint;
    private String destinationPoint;
    private int freeSeats;
    private Set<String> companions;
    private LocalDateTime departureDate;
    private String comment;
    private Status status;

    public Ride() {
        setStatus(Status.INIT);
    }

    public Ride(String ownerId, String departurePoint, String destinationPoint, int freeSeats, LocalDateTime departureDate, String comment, Status status) {
        this.ownerId = ownerId;
        this.departurePoint = departurePoint;
        this.destinationPoint = destinationPoint;
        this.freeSeats = freeSeats;
        this.departureDate = departureDate;
        this.comment = comment;
        this.status = status;
    }

    public static Ride.RideBuilder builder() {
        return new Ride.RideBuilder();
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getDeparturePoint() {
        return departurePoint;
    }

    public void setDeparturePoint(String departurePoint) {
        this.departurePoint = departurePoint;
    }

    public String getDestinationPoint() {
        return destinationPoint;
    }

    public void setDestinationPoint(String destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    public int getFreeSeats() {
        return freeSeats - getCompanions().size();
    }

    public void setFreeSeats(int freeSeats) {
        this.freeSeats = freeSeats;
    }

    public Set<String> getCompanions() {
        if (companions == null) {
            companions = new HashSet<>();
        }
        return companions;
    }

    public void addCompanion(String userId) {
        getCompanions().add(userId);
    }

    public LocalDateTime getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDateTime departureDate) {
        this.departureDate = departureDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        INIT,
        PENDING_CONFIRMATION,
        READY,
        CANCELLED,
        STALE,
        DELETED
    }

    @Override
    public String toString() {
        return "Ride{" +
            "id='" + id + '\'' +
            ", createdDate=" + createdDate +
            ", lastModifiedDate=" + lastModifiedDate +
            ", ownerId='" + ownerId + '\'' +
            ", departurePoint='" + departurePoint + '\'' +
            ", destinationPoint='" + destinationPoint + '\'' +
            ", freeSeats=" + freeSeats +
            ", companions=" + companions +
            ", departureDate=" + departureDate +
            ", comment='" + comment + '\'' +
            ", status=" + status +
            '}';
    }

    public static class RideBuilder {
        private String ownerId;
        private String departurePoint;
        private String destinationPoint;
        private int freeSeats;
        private LocalDateTime departureDate;
        private String comment;
        private Status status;

        public Ride.RideBuilder setOwnerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Ride.RideBuilder setDeparturePoint(String departurePoint) {
            this.departurePoint = departurePoint;
            return this;
        }

        public Ride.RideBuilder setDestinationPoint(String destinationPoint) {
            this.destinationPoint = destinationPoint;
            return this;
        }

        public Ride.RideBuilder setFreeSeats(int freeSeats) {
            this.freeSeats = freeSeats;
            return this;
        }

        public Ride.RideBuilder setDepartureDate(LocalDateTime departureDate) {
            this.departureDate = departureDate;
            return this;
        }

        public Ride.RideBuilder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Ride.RideBuilder setStatus(Status status) {
            this.status = status;
            return this;
        }

        public Ride build() {
            return new Ride(ownerId, departurePoint, destinationPoint, freeSeats, departureDate, comment, status);
        }
    }
}
