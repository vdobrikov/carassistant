package com.carassistant.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @NotNull
    private Role role = Role.USER;

    private SlackInfo slackInfo = new SlackInfo();
    private String email;
    private String location;

    /**
     * Use map to store properties which are not used to search user by
     */
    @NotNull
    private Map<String, Object> properties = new HashMap<>();

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return Role.ADMIN == role;
    }

    public SlackInfo getSlackInfo() {
        return slackInfo;
    }

    public void setSlackInfo(SlackInfo slackInfo) {
        this.slackInfo = slackInfo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperty(String name, Object value) {
        getProperties().put(name, value);
    }

    public Object getProperty(String name) {
        return getProperties().get(name);
    }

    public enum Role {
        USER,
        ADMIN;
    }


    public enum Properties {
        LAST_SHARED_RIDE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!id.equals(user.id)) return false;
        if (role != user.role) return false;
        if (slackInfo != null ? !slackInfo.equals(user.slackInfo) : user.slackInfo != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        return location.equals(user.location);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
            "id='" + id + '\'' +
            ", createdDate=" + createdDate +
            ", lastModifiedDate=" + lastModifiedDate +
            ", role=" + role +
            ", slackInfo='" + slackInfo + '\'' +
            ", email='" + email + '\'' +
            ", location='" + location + '\'' +
            ", properties=" + properties +
            '}';
    }
}
