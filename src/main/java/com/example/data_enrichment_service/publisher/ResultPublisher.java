package com.example.data_enrichment_service.publisher;

import com.example.data_enrichment_service.config.properties.RabbitMqProperties;
import com.example.data_enrichment_service.dto.ResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes the enrichment result to the outbound exchange.
 * <p>
 * Called only after the DB transaction has committed (see {@link EnrichmentService}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResultPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    public void publish(ResultMessage message) {
        rabbitTemplate.convertAndSend(properties.outboundExchange(), properties.outboundRoutingKey(), message);

        log.info("Published result: logId={}, messageId={}, result={}", message.logId(), message.messageId(), message.result());
    }
}
