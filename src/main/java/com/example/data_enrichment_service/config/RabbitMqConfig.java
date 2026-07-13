package com.example.data_enrichment_service.config;

import com.example.data_enrichment_service.config.properties.RabbitMqProperties;
import com.example.data_enrichment_service.exception.NonRetryableEnrichmentException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitListenerRetrySettingsCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMqConfig {

    private final RabbitMqProperties properties;

    @Bean
    Queue inboundQueue() {
        return QueueBuilder.durable(properties.inboundQueue())
                .deadLetterExchange(properties.inboundDlx())
                .build();
    }

    @Bean
    FanoutExchange deadLetterExchange() {
        return new FanoutExchange(properties.inboundDlx(), true, false);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder
                .durable(properties.inboundDlq())
                .build();
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange());
    }

    @Bean
    DirectExchange outboundExchange() {
        return new DirectExchange(properties.outboundExchange(), true, false);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter("com.example.data_enrichment_service.dto");
    }

    @Bean
    RabbitListenerRetrySettingsCustomizer nonRetryableExceptionsCustomizer() {
        return settings -> settings.getExceptionExcludes()
                .add(NonRetryableEnrichmentException.class);
    }
}
