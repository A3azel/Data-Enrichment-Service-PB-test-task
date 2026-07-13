package com.example.data_enrichment_service.service;

import com.example.data_enrichment_service.client.EnrichmentClient;
import com.example.data_enrichment_service.client.dto.EnrichmentRequest;
import com.example.data_enrichment_service.client.dto.EnrichmentResponse;
import com.example.data_enrichment_service.dto.IncomingMessage;
import com.example.data_enrichment_service.dto.ResultMessage;
import com.example.data_enrichment_service.entity.EnrichmentResult;
import com.example.data_enrichment_service.exception.EnrichmentApiException;
import com.example.data_enrichment_service.publisher.ResultPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrichmentServiceTest {

    @Mock
    private EnrichmentClient enrichmentClient;
    @Mock
    private ResultPersistenceService persistenceService;
    @Mock
    private ResultPublisher resultPublisher;

    @InjectMocks
    private EnrichmentService enrichmentService;

    private IncomingMessage message;

    @BeforeEach
    void setUp() {
        message = new IncomingMessage(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                42L, "LOGIN",
                LocalDateTime.of(2026, 7, 1, 10, 0, 0));
    }

    @Test
    void processesNewMessage_savesAndPublishes() {
        when(persistenceService.isMessageAlreadyExists(message.messageId())).thenReturn(false);
        when(enrichmentClient.enrich(any(EnrichmentRequest.class)))
                .thenReturn(new EnrichmentResponse(42L, true));
        when(persistenceService.save(any(), any()))
                .thenReturn(EnrichmentResult.builder()
                        .id(100L)
                        .messageId(message.messageId())
                        .userId(42L)
                        .action("LOGIN")
                        .result(true)
                        .build());

        enrichmentService.process(message);

        ArgumentCaptor<ResultMessage> captor = ArgumentCaptor.forClass(ResultMessage.class);
        verify(resultPublisher).publish(captor.capture());

        ResultMessage published = captor.getValue();
        assertThat(published.logId()).isEqualTo(100L);
        assertThat(published.messageId()).isEqualTo(message.messageId());
        assertThat(published.result()).isTrue();
    }

    @Test
    void skipsDuplicate_detectedByPreCheck() {
        when(persistenceService.isMessageAlreadyExists(message.messageId())).thenReturn(true);

        enrichmentService.process(message);

        verify(persistenceService, never()).save(any(), any());
        verifyNoInteractions(enrichmentClient, resultPublisher);
    }

    @Test
    void propagatesApiFailure_withoutSavingOrPublishing() {
        when(persistenceService.isMessageAlreadyExists(message.messageId())).thenReturn(false);
        when(enrichmentClient.enrich(any(EnrichmentRequest.class)))
                .thenThrow(new EnrichmentApiException("service unavailable"));

        assertThatThrownBy(() -> enrichmentService.process(message))
                .isInstanceOf(EnrichmentApiException.class);

        verify(persistenceService, never()).save(any(), any());
        verifyNoInteractions(resultPublisher);
    }

    @Test
    void skipsDuplicate_detectedByUniqueConstraint() {
        when(persistenceService.isMessageAlreadyExists(message.messageId())).thenReturn(false);
        when(enrichmentClient.enrich(any(EnrichmentRequest.class)))
                .thenReturn(new EnrichmentResponse(42L, true));
        when(persistenceService.save(any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        enrichmentService.process(message);

        verifyNoInteractions(resultPublisher);
    }
}
