package com.crm.payroll.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.payroll-events:payroll-events}")
    private String payrollEventsTopic;

    @Bean
    public NewTopic payrollEventsTopic() {
        return TopicBuilder.name(payrollEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
