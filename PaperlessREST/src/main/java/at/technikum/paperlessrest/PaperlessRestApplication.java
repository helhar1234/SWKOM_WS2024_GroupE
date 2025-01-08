package at.technikum.paperlessrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "at.technikum.paperlessrest.repository")
@SpringBootApplication
public class PaperlessRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperlessRestApplication.class, args);
    }

}
