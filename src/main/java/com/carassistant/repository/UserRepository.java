package com.carassistant.repository;

import com.carassistant.model.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String> {
    User findOneBySlackInfoUserId(String slackId);
}
