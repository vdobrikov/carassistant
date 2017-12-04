package com.carassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

// https://github.com/ramswaroop/jbot  // WebSocket client only, focused on chat functionality
// https://github.com/seratch/jslack  // Each request must be provided with access token via builder, cannot be migrated to bean
// https://github.com/Ullink/simple-slack-api  // WebSocket client only
// https://github.com/allbegray/slack-api // No dialogs support
// TODO Add support for "path variable" for callback ID (like Spring has for URI)
// TODO Fix JSlack lib: confirmation -> confirm, refactor requests
// TODO i18n
// TODO Tests

// App config: https://api.slack.com/apps

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
