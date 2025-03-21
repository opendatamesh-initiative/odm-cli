package org.opendatamesh.cli.clients;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfigs {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        long timeout = 180;
        return builder
                .setConnectTimeout(Duration.ofSeconds(timeout))
                .setReadTimeout(Duration.ofSeconds(timeout))
                .build();
    }

}
