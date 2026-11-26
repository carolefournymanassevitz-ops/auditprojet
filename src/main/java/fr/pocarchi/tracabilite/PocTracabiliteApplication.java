package fr.pocarchi.tracabilite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PocTracabiliteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PocTracabiliteApplication.class, args);
    }
}
