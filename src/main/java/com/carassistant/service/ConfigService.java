package com.carassistant.service;

import com.carassistant.model.Config;

import java.util.Map;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public interface ConfigService {
    Optional<Config> findByName(String name);
    Config save(Config config);
    void delete(Config config);

    Map<String, String> getLocations();
}
