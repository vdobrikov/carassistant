package com.carassistant.service;

import com.carassistant.exception.UserNotFoundException;
import com.carassistant.model.User;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface UserService {
    Iterable<User> findAll();
    User findById(String id) throws UserNotFoundException;
    User findOneBySlackInfoUserId(String slackId) throws UserNotFoundException;
    User findOneBySlackInfoUserIdWithoutException(String slackId);
    User save(User user);
    void delete(User user);
}
