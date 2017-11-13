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
    private String pointFrom;
    private String pointTo;
    private Set<String> waypoints;
    private int seats;
    private Set<String> companions;
    private LocalDateTime date;
    private String comment;
    private State state;

    private boolean exclusive;
    private Set<String> candidates;

    public Ride() {
        setState(State.INIT);
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

    public String getPointFrom() {
        return pointFrom;
    }

    public void setPointFrom(String pointFrom) {
        this.pointFrom = pointFrom;
    }

    public String getPointTo() {
        return pointTo;
    }

    public void setPointTo(String pointTo) {
        this.pointTo = pointTo;
    }

    public Set<String> getWaypoints() {
        if (waypoints == null) {
            waypoints = new HashSet<>();
        }
        return waypoints;
    }

    public void setWaypoints(Set<String> waypoints) {
        this.waypoints = waypoints;
    }

    public int getSeats() {
        return seats - getCompanions().size();
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public Set<String> getCompanions() {
        if (companions == null) {
            companions = new HashSet<>();
        }
        return companions;
    }

    public void setCompanions(Set<String> companions) {
        this.companions = companions;
    }

    public void addCompanion(String userId) {
        getCompanions().add(userId);
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public Set<String> getCandidates() {
        if (candidates == null) {
            candidates = new HashSet<>();
        }
        return candidates;
    }

    public void setCandidates(Set<String> candidates) {
        this.candidates = candidates;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {
        INIT,
        ASK_FROM,
        SAVE_FROM_AND_ASK_TO,
        SAVE_TO_AND_ASK_DATE,
        SAVE_DATE_AND_ASK_SEATS,
        SAVE_SEATS_AND_COMPLETE,
        READY
    }
}
