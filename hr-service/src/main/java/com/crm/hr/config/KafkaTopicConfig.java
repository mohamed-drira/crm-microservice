package com.crm.hr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.employee-events:employee-events}")
    private String employeeEventsTopic;

    @Bean
    public NewTopic employeeEventsTopic() {
        return TopicBuilder.name(employeeEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
