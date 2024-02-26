package edu.usd.jbuchan.ipheatmap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class IpheatmapApplication implements CommandLineRunner {

	@Autowired
	TsharkHandlerService tsharkHandlerService;
	public static void main(String[] args) {
		SpringApplication.run(IpheatmapApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		tsharkHandlerService.run();
	}
}
