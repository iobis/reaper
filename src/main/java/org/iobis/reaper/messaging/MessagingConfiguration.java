package org.iobis.reaper.messaging;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class MessagingConfiguration {

    private final Properties producerProperties;

    @Autowired
    public MessagingConfiguration(Properties producerProperties) {
        this.producerProperties = producerProperties;
    }

    @Bean
    public Producer<String,String> producer() {
        return new KafkaProducer<String, String>(producerProperties);
    }


}
