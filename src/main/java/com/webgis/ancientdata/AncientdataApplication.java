package com.webgis.ancientdata;

import com.fasterxml.jackson.core.StreamWriteConstraints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AncientdataApplication {

	public static void main(String[] args) {
		SpringApplication.run(AncientdataApplication.class, args);
		System.out.println("yes, Server app running");
	}
}
