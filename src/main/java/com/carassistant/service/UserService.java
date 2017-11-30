package com.carassistant.service;

import com.carassistant.exception.UserNotFoundException;
import com.carassistant.model.User;

import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface UserService {
    Iterable<User> findAll();
    Optional<User> findById(String id) throws UserNotFoundException;
    Optional<User> findOneBySlackInfoUserId(String slackId) throws UserNotFoundException;
    User save(User user);
    void delete(User user);
}
