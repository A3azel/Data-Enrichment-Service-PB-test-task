package com.example.data_enrichment_service.service;

import com.example.data_enrichment_service.client.EnrichmentClient;
import com.example.data_enrichment_service.client.dto.EnrichmentRequest;
import com.example.data_enrichment_service.client.dto.EnrichmentResponse;
import com.example.data_enrichment_service.dto.IncomingMessage;
import com.example.data_enrichment_service.dto.ResultMessage;
import com.example.data_enrichment_service.entity.EnrichmentResult;
import com.example.data_enrichment_service.publisher.ResultPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrichmentService {

    private final EnrichmentClient enrichmentClient;
    private final ResultPersistenceService persistenceService;
    private final ResultPublisher resultPublisher;

    /**
     * Processes a single inbound message:
     * <ol>
     *   <li><b>Duplicate check</b> - known {@code messageId} -> skip</li>
     *   <li><b>Enrich</b> - external API call, outside any transaction.</li>
     *   <li><b>Persist</b> - the transactional step ({@link ResultPersistenceService}).</li>
     *   <li><b>Publish</b> - runs after that call returns</li>
     * </ol>
     */
    public void process(IncomingMessage message) {
        if (persistenceService.isMessageAlreadyExists(message.messageId())) {
            log.info("Duplicate messageId={}, skipping (pre-check)", message.messageId());
            return;
        }

        EnrichmentResponse enrichment = enrichmentClient.enrich(new EnrichmentRequest(message.userId(), message.action()));

        EnrichmentResult saved;
        try {
            saved = persistenceService.save(message, enrichment);
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate messageId={}, skipping (unique constraint)", message.messageId());
            return;
        }

        resultPublisher.publish(new ResultMessage(saved.getId(), message.messageId(), enrichment.result()));
    }
}
