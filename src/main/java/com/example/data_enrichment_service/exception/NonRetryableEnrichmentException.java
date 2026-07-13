package com.example.data_enrichment_service.exception;

public class NonRetryableEnrichmentException extends EnrichmentApiException {

    public NonRetryableEnrichmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
