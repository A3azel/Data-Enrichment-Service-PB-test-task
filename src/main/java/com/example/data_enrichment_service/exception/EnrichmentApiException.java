package com.example.data_enrichment_service.exception;

public class EnrichmentApiException extends RuntimeException {

    public EnrichmentApiException(String message) {
        super(message);
    }

    public EnrichmentApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
