package com.carassistant.event.user;

import com.carassistant.event.CarAssistantEvent;
import com.carassistant.model.User;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class UserDeletedEvent extends CarAssistantEvent {
    private User user;

    public UserDeletedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "UserDeletedEvent{" +
            "user=" + user +
            "} " + super.toString();
    }
}
