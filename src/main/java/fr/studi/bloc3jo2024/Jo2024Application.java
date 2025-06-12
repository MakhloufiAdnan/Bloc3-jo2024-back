package fr.studi.bloc3jo2024;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Jo2024Application {

	private static final Logger log = LoggerFactory.getLogger(Jo2024Application.class);
    public static void main(String[] args) {
		SpringApplication.run(Jo2024Application.class, args);
	}

	static {
		log.info("=== Lancement application Jo2024 ===");
	}
}