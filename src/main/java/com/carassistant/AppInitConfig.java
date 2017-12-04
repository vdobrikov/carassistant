package com.carassistant;

import com.carassistant.model.Config;
import com.carassistant.service.ConfigService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@Configuration
public class AppInitConfig {
    @Bean
    InitializingBean initDatabase(ConfigService configService, @Value("#{${carassistant.init.data.config.locations}}") Map<String,String> locations) {
        return () -> {
            configService.save(new Config("locations", locations));
        };
    }
}
