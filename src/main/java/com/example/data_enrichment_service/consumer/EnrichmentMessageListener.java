package com.example.data_enrichment_service.consumer;

import com.example.data_enrichment_service.dto.IncomingMessage;
import com.example.data_enrichment_service.service.EnrichmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Entry point of the flow: consumes from the inbound queue and delegates to
 * {@link EnrichmentService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnrichmentMessageListener {

    private final EnrichmentService enrichmentService;

    @RabbitListener(queues = "${app.rabbitmq.inbound-queue}")
    public void onMessage(IncomingMessage message) {
        log.info("Received messageId={}, userId={}", message.messageId(), message.userId());
        enrichmentService.process(message);
    }

}
