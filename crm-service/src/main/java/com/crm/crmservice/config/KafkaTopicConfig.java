package com.crm.crmservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.employee-events:employee-events}")
    private String employeeEventsTopic;

    @Value("${app.kafka.topic.invoice-events:invoice-events}")
    private String invoiceEventsTopic;

    @Bean
    public NewTopic employeeEventsTopic() {
        return TopicBuilder.name(employeeEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic invoiceEventsTopic() {
        return TopicBuilder.name(invoiceEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}