package com.example.data_enrichment_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.enrichment-api")
public record EnrichmentApiProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
