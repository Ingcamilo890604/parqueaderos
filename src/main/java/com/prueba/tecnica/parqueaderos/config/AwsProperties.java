package com.prueba.tecnica.parqueaderos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "amazon.aws")
public class AwsProperties {
    private String accesskey;
    private String secretkey;
    private String arnentryparking;

    private String arnexitparking;

    public String getAccessKey() {
        return accesskey;
    }

    public void setAccessKey(String accessKey) {
        this.accesskey = accessKey;
    }

    public String getSecretKey() {
        return secretkey;
    }

    public void setSecretKey(String secretKey) {
        this.secretkey = secretKey;
    }

    public String getArnentryparking() {
        return arnentryparking;
    }

    public void setArnentryparking(String arnentryparking) {
        this.arnentryparking = arnentryparking;
    }

    public String getArnexitparking() {
        return arnexitparking;
    }

    public void setArnexitparking(String arnexitparking) {
        this.arnexitparking = arnexitparking;
    }
}
