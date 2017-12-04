package com.carassistant.service;

import com.carassistant.event.user.UserCreatedEvent;
import com.carassistant.event.user.UserDeletedEvent;
import com.carassistant.model.User;
import com.carassistant.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserRepository userRepository;
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(String id) {
        User user = userRepository.findOne(id);
        if (user == null) {
            LOG.warn("User not found: id={}", id);
        }
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findOneBySlackInfoUserId(String slackId) {
        User user = userRepository.findOneBySlackInfoUserId(slackId);
        if (user == null) {
            LOG.warn("User not found: slackId={}", slackId);
        }
        return Optional.ofNullable(user);
    }

    @Override
    public User save(User user) {
        String idBeforeSave = user.getId();
        user = userRepository.save(user);
        if (idBeforeSave == null && user.getId() != null) {
            eventPublisher.publishEvent(new UserCreatedEvent(user));
        }
        return user;
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
        eventPublisher.publishEvent(new UserDeletedEvent(user));
    }
}
