package com.webgis.ancientdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AncientdataApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(AncientdataApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AncientdataApplication.class, args);
		LOGGER.info("AncientDataWebGIS server is running");
	}
}
