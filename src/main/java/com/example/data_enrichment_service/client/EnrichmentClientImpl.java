package com.example.data_enrichment_service.client;

import com.example.data_enrichment_service.client.dto.EnrichmentRequest;
import com.example.data_enrichment_service.client.dto.EnrichmentResponse;
import com.example.data_enrichment_service.exception.EnrichmentApiException;
import com.example.data_enrichment_service.exception.NonRetryableEnrichmentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Real implementation: performs the HTTP POST to the external enrichment service.
 * Active by default ({@code app.enrichment-api.stub=false} or absent).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.enrichment-api.stub", havingValue = "false", matchIfMissing = true)
public class EnrichmentClientImpl implements EnrichmentClient {

    private static final String ENRICH_PATH = "/enrich";

    private final RestClient enrichmentRestClient;

    public EnrichmentClientImpl(RestClient enrichmentRestClient) {
        this.enrichmentRestClient = enrichmentRestClient;
    }

    @Override
    public EnrichmentResponse enrich(EnrichmentRequest request) {
        try {
            EnrichmentResponse response = enrichmentRestClient.post()
                    .uri(ENRICH_PATH)
                    .body(request)
                    .retrieve()
                    .body(EnrichmentResponse.class);

            if (response == null) {
                throw new EnrichmentApiException("Enrichment service returned empty body for userId=" + request.userId());
            }
            return response;

        } catch (HttpClientErrorException e) {
            throw new NonRetryableEnrichmentException("Enrichment rejected request (4xx) for userId=" + request.userId(), e);

        } catch (RestClientException e) {
            throw new EnrichmentApiException("Enrichment call failed for userId=" + request.userId(), e);
        }
    }
}
