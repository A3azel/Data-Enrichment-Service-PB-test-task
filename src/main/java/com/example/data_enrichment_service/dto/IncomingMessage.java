package com.example.data_enrichment_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncomingMessage(
        UUID messageId,
        Long userId,
        String action,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.S")
        LocalDateTime timestamp
) {
}
