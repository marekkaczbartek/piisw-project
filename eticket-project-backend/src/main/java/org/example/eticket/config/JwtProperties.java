package org.example.eticket.config;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
@Value
public class JwtProperties {

    String secret;
    String issuer;
    long expirationMinutes;
}

