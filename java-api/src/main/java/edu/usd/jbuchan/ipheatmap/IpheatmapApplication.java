package edu.usd.jbuchan.ipheatmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.commons.cli.*;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Slf4j
@EnableAsync
public class IpheatmapApplication {

	@Autowired
	TsharkHandlerService tsharkHandlerService;

	public static void main(String[] args) {
		SpringApplication.run(IpheatmapApplication.class, args);
	}

	@PostConstruct
	public void init() throws Exception {
		String networkInterface = System.getenv("network.interface");
		log.info("interface is {}",networkInterface);
		if (networkInterface == null) {
			throw new IllegalArgumentException("Network interface not specified. Please use -Dnetwork.interface to specify it.");
		}
		tsharkHandlerService.run(networkInterface);
	}
}
