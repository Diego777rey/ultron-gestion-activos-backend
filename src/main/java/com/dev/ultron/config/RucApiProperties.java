package com.dev.ultron.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Servicio externo de consulta de contribuyentes (datos públicos de la DNIT).
 */
@Component
@ConfigurationProperties(prefix = "ruc.api")
@Data
public class RucApiProperties {
    private String baseUrl = "https://turuc.com.py/api";
    private Duration timeout = Duration.ofSeconds(5);
}
