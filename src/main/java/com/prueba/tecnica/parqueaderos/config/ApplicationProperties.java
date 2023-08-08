package com.prueba.tecnica.parqueaderos.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.config")
@Data
public class ApplicationProperties {
    private String profile;
    private String url;
    private String notificationService;
}

