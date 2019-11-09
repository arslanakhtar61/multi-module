package com.example.ibm.mq.camel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class IbmMqCamelWarApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(IbmMqCamelWarApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(IbmMqCamelWarApplication.class);
	}

	@RestController
	public static class WarInitializerController {

		@GetMapping("/")
		public String handler() {
			return("Hi from IbmMqCamelWarApplication");
		}
	}

}
