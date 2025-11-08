package com.firststudent.platform.viviendasmartbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ViviendaSmartBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ViviendaSmartBackendApplication.class, args);
    }

}
