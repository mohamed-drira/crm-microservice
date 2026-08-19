package com.crm.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.employee-events:employee-events}")
    private String employeeEventsTopic;

    @Value("${app.kafka.topic.payroll-events:payroll-events}")
    private String payrollEventsTopic;

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
    public NewTopic payrollEventsTopic() {
        return TopicBuilder.name(payrollEventsTopic)
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

    @Bean
    public NewTopic employeeEventsDlt() {
        return TopicBuilder.name(employeeEventsTopic + "-dlt")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic payrollEventsDlt() {
        return TopicBuilder.name(payrollEventsTopic + "-dlt")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic invoiceEventsDlt() {
        return TopicBuilder.name(invoiceEventsTopic + "-dlt")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(1000L, 3L));
    }
}
