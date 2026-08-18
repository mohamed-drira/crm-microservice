package com.crm.gateway.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditEventProducer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventProducer.class);

    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;
    private final String topic;

    public AuditEventProducer(KafkaTemplate<String, AuditEvent> kafkaTemplate,
                              @Value("${app.kafka.topic.audit-events:audit-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(AuditEvent event) {
        kafkaTemplate.send(topic, event.eventId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish audit event: {}", ex.getMessage());
                    } else {
                        log.debug("Published audit event to {} (offset {})", topic,
                                result != null ? result.getRecordMetadata().offset() : "n/a");
                    }
                });
    }
}
