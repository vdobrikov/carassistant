package com.carbot.carbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"me.ramswaroop.jbot", "com.carbot"})
public class CarbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarbotApplication.class, args);
	}
}
