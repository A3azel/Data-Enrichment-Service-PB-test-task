package com.example.data_enrichment_service.client;

import com.example.data_enrichment_service.client.dto.EnrichmentRequest;
import com.example.data_enrichment_service.client.dto.EnrichmentResponse;

public interface EnrichmentClient {
    /**
     * @throws NonRetryableEnrichmentException on 4xx
     * @throws EnrichmentApiException on 5xx
     */
    EnrichmentResponse enrich(EnrichmentRequest request);
}
