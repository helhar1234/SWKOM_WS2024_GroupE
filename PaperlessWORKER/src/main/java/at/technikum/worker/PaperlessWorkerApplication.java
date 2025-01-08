package at.technikum.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaperlessWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperlessWorkerApplication.class, args);
        System.out.println("Worker Service gestartet...");
    }
}
