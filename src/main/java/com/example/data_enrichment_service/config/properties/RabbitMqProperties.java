package com.example.data_enrichment_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMqProperties(
        String inboundQueue,
        String inboundDlx,
        String inboundDlq,
        String outboundExchange,
        String outboundRoutingKey
) {
}
