package com.exampleIbm.mq.camel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = {"com.example.ibm.mq.camel"})
public class DispatcherApplication extends SpringBootServletInitializer {

	private static final Logger log = LogManager.getLogger(DispatcherApplication.class);

	public static void main(String[] args) {

		try {
			ApplicationContext ctx = SpringApplication.run(DispatcherApplication.class, args);
			String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
			for (String profile: activeProfiles) {
				log.info("Spring Boot Profile:{}", profile);
			}
		} catch (Exception ex) {
			log.error("DispatcherApplication start failed!", ex);
		}
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(DispatcherApplication.class);
	}


}
