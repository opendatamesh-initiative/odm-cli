package org.opendatamesh.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OdmCliApplication {
    public static void main(String[] args) {
        SpringApplication.run(OdmCliApplication.class, args);
    }
}
