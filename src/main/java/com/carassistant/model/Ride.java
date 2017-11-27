package com.carassistant.model;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @NotBlank
    private String location;

    @NotNull
    @DBRef
    private User owner;

    @NotBlank
    private String departurePoint;

    private String destinationPoint;

    @Min(1)
    private int freeSeats;

    @NotNull
    @DBRef
    private List<User> companions;

    private LocalDateTime departureDate;

    private String comment;

    @NotNull
    private Status status;

    public Ride() {
        setStatus(Status.INIT);
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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

    public List<User> getCompanions() {
        if (companions == null) {
            companions = new ArrayList<>();
        }
        return companions;
    }

    public void addCompanion(User user) {
        Assert.isTrue(getFreeSeats() > 0, "Cannot add companion: no free seats available");
        Assert.notNull(user, "'user' cannot be null");
        getCompanions().add(user);
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
        NOTIFIED_OWNER,
        NOTIFIED_COMPANIONS,
        CANCELLED,
        STALE,
        DELETED,
        DELETED_COMPLETELY // Don't store it to DB, system event
    }

    @Override
    public String toString() {
        return "Ride{" +
            "id='" + id + '\'' +
            ", createdDate=" + createdDate +
            ", lastModifiedDate=" + lastModifiedDate +
            ", owner='" + owner + '\'' +
            ", departurePoint='" + departurePoint + '\'' +
            ", destinationPoint='" + destinationPoint + '\'' +
            ", freeSeats=" + freeSeats +
            ", companions=" + companions +
            ", departureDate=" + departureDate +
            ", comment='" + comment + '\'' +
            ", status=" + status +
            '}';
    }
}
