package com.example.data_enrichment_service.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Takes a raw {@link Message} on purpose: the DLQ often holds messages that failed to
 * deserialize, so a typed parameter would fail again and hide the payload.
 */
@Slf4j
@Component
public class DeadLetterListener {

    @RabbitListener(queues = "${app.rabbitmq.inbound-dlq}")
    public void onDeadLetter(Message message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        log.error("Dead-lettered message: body={}, x-death={}, contentType={}",
                body,
                message.getMessageProperties().getXDeathHeader(),
                message.getMessageProperties().getContentType());
    }
}
