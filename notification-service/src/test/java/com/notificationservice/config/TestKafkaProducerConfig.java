package com.notificationservice.config;

import com.notificationservice.event.UserEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

@TestConfiguration
public class TestKafkaProducerConfig {

    @Bean
    @Primary
    public ProducerFactory<String, UserEvent> testProducerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
        Map<String, Object> configs = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, UserEvent> testKafkaTemplate(ProducerFactory<String, UserEvent> testProducerFactory) {
        return new KafkaTemplate<>(testProducerFactory);
    }
}