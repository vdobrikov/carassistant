package com.carassistant.carbot.slack.framework;

import com.carassistant.model.User;
import com.carassistant.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContext {
    private UserService userService;
    private User user;

    @Autowired
    public UserContext(UserService userService) {
        this.userService = userService;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        Assert.isNull(this.user, "'user' is immutable, thus cannot be set 2+ times");
        this.user = user;
    }

    public boolean hasUser() {
        return user != null;
    }

    public User setUserFromSlackIdIfExists(String slackUserId) {
        setUser(userService.findOneBySlackInfoUserIdWithoutException(slackUserId));
        return getUser();
    }
}
