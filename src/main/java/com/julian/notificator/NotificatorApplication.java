package com.julian.notificator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NotificatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificatorApplication.class, args);
	}

}
