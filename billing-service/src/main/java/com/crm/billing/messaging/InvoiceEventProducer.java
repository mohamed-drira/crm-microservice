package com.crm.billing.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEventProducer.class);

    private final KafkaTemplate<String, InvoiceEvent> kafkaTemplate;
    private final String topic;

    public InvoiceEventProducer(KafkaTemplate<String, InvoiceEvent> kafkaTemplate,
                                @Value("${app.kafka.topic.invoice-events:invoice-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(InvoiceEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.getInvoiceId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish invoice event to topic {}: {}", topic, ex.getMessage());
                    } else {
                        log.debug("Published {} to {} (offset {})",
                                event.getType(), topic,
                                result != null ? result.getRecordMetadata().offset() : "n/a");
                    }
                });
    }
}