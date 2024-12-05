package at.technikum.paperlessrest;

import org.apache.logging.log4j.LogManager; //did not have an effect
import org.apache.logging.log4j.Logger; //did not have an effect

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaperlessRestApplication {
    private static final Logger logger = LogManager.getLogger(PaperlessRestApplication.class); //no effect
    public static void main(String[] args) {
        SpringApplication.run(PaperlessRestApplication.class, args);
    }

}
