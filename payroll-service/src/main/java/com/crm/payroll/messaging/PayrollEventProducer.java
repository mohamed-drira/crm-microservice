package com.crm.payroll.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PayrollEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PayrollEventProducer.class);

    private final KafkaTemplate<String, PayrollEvent> kafkaTemplate;
    private final String topic;

    public PayrollEventProducer(KafkaTemplate<String, PayrollEvent> kafkaTemplate,
                                @Value("${app.kafka.topic.payroll-events:payroll-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(PayrollEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.getEmployeeId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish payroll event to topic {}: {}", topic, ex.getMessage());
                    } else {
                        log.debug("Published {} to {} (offset {})",
                                event.getType(), topic,
                                result != null ? result.getRecordMetadata().offset() : "n/a");
                    }
                });
    }
}
