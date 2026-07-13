package com.example.data_enrichment_service.client;

import com.example.data_enrichment_service.client.dto.EnrichmentRequest;
import com.example.data_enrichment_service.client.dto.EnrichmentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub implementation — makes no HTTP calls, always returns a positive result.
 * Active when {@code app.enrichment-api.stub=true}.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.enrichment-api.stub", havingValue = "true")
public class StubEnrichmentClientImpl implements EnrichmentClient {

    @Override
    public EnrichmentResponse enrich(EnrichmentRequest request) {
        log.info("Stub enrichment: userId={}, action={}", request.userId(), request.action());
        return new EnrichmentResponse(request.userId(), true);
    }
}
