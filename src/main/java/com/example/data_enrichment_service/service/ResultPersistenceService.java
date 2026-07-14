package com.example.data_enrichment_service.service;

import com.example.data_enrichment_service.client.dto.EnrichmentResponse;
import com.example.data_enrichment_service.dto.IncomingMessage;
import com.example.data_enrichment_service.entity.EnrichmentResult;
import com.example.data_enrichment_service.repository.EnrichmentResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResultPersistenceService {

    private final EnrichmentResultRepository repository;

    @Transactional
    public EnrichmentResult save(IncomingMessage message, EnrichmentResponse enrichment) {
        EnrichmentResult entity = EnrichmentResult.builder()
                .messageId(message.messageId())
                .userId(message.userId())
                .action(message.action())
                .result(enrichment.result())
                .eventTime(message.timestamp())
                .build();
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public boolean isMessageAlreadyExists(UUID messageId) {
        return repository.existsByMessageId(messageId);
    }
}