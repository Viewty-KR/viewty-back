package com.viewty.viewtyback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableCaching
@SpringBootApplication
@EnableJpaAuditing
public class ViewtyBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ViewtyBackApplication.class, args);
	}

}
