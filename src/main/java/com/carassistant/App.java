package com.carassistant;

import com.carassistant.model.Config;
import com.carassistant.service.ConfigService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

// https://github.com/ramswaroop/jbot  // WebSocket client only, focused on chat functionality
// https://github.com/seratch/jslack  // Each request must be provided with access token via builder, cannot be migrated to bean
// https://github.com/Ullink/simple-slack-api  // WebSocket client only
// https://github.com/allbegray/slack-api // No dialogs support
// TODO: Fix JSlack lib: confirmation -> confirm, refactor requests
// TODO: i18n
// TODO: Tests

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
public class App {

	@Bean
	InitializingBean initDatabase(ConfigService configService, @Value("#{${init.data.config.locations}}") Map<String,String> locations) {
		return () -> {
			configService.save(new Config("locations", locations));
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
