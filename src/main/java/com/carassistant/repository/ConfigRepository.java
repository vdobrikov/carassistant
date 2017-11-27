package com.carassistant.repository;

import com.carassistant.model.Config;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Repository
public interface ConfigRepository extends PagingAndSortingRepository<Config, String> {
}
