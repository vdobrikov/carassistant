package com.carassistant.service;

import com.carassistant.model.Config;
import com.carassistant.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Service
public class ConfigServiceImpl implements ConfigService {
    public static final String CONFIG_LOCATIONS = "locations";

    private ConfigRepository configRepository;

    @Autowired
    public ConfigServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public Optional<Config> findByName(String name) {
        return Optional.ofNullable(configRepository.findOne(name));
    }

    @Override
    public Config save(Config config) {
        return configRepository.save(config);
    }

    @Override
    public void delete(Config config) {
        configRepository.delete(config);
    }

    @Override
    public Map<String, String> getLocations() {
        Optional<Config> locationsOptional = findByName(CONFIG_LOCATIONS);
        Map<String, String> locations;
        if (!locationsOptional.isPresent()) {
            locations = new HashMap<>();
            configRepository.save(new Config(CONFIG_LOCATIONS, locations));
            return locations;
        }
        Assert.isInstanceOf(Map.class, locationsOptional.get().getValue(), "'locations' config should hold Map<String, String>");
        return (Map<String, String>) locationsOptional.get().getValue();
    }
}
