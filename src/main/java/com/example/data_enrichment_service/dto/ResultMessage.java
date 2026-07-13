package com.example.data_enrichment_service.dto;

import java.util.UUID;

public record ResultMessage(Long logId, UUID messageId, boolean result) {
}
