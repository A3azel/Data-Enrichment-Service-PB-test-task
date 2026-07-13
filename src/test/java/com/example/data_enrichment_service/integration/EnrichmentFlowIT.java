package com.example.data_enrichment_service.integration;

import com.example.data_enrichment_service.TestcontainersConfiguration;
import com.example.data_enrichment_service.config.properties.RabbitMqProperties;
import com.example.data_enrichment_service.dto.IncomingMessage;
import com.example.data_enrichment_service.dto.ResultMessage;
import com.example.data_enrichment_service.repository.EnrichmentResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "app.enrichment-api.stub=true",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(TestcontainersConfiguration.class)
class EnrichmentFlowIT {

    private static final String CAPTURE_QUEUE = "test.capture.queue";

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RabbitAdmin rabbitAdmin;
    @Autowired
    EnrichmentResultRepository repository;
    @Autowired
    RabbitMqProperties props;

    @BeforeEach
    void setUpCaptureQueue() {
        Queue captureQueue = new Queue(CAPTURE_QUEUE, true);
        DirectExchange outbound = new DirectExchange(props.outboundExchange(), true, false);

        rabbitAdmin.declareExchange(outbound);
        rabbitAdmin.declareQueue(captureQueue);
        rabbitAdmin.declareBinding(
                BindingBuilder.bind(captureQueue)
                        .to(outbound)
                        .with(props.outboundRoutingKey()));
    }

    @Test
    void endToEnd_savesRowAndPublishesResult() {
        IncomingMessage message = new IncomingMessage(
                UUID.randomUUID(), 42L, "LOGIN",
                LocalDateTime.of(2026, 7, 1, 10, 0, 0));

        rabbitTemplate.convertAndSend(props.inboundQueue(), message);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(repository.existsByMessageId(message.messageId())).isTrue());

        Object received = rabbitTemplate.receiveAndConvert(CAPTURE_QUEUE, 5_000);

        assertThat(received).isInstanceOf(ResultMessage.class);
        ResultMessage result = (ResultMessage) received;
        assertThat(Objects.requireNonNull(result).messageId()).isEqualTo(message.messageId());
        assertThat(result.result()).isTrue();
    }

}
