package com.crm.crmservice.messaging;

import com.crm.crmservice.entity.Contact;
import com.crm.crmservice.repository.ContactRepository;
import com.crm.crmservice.service.InteractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEventConsumer.class);

    private final ContactRepository contactRepository;
    private final InteractionService interactionService;

    public InvoiceEventConsumer(ContactRepository contactRepository,
                                InteractionService interactionService) {
        this.contactRepository = contactRepository;
        this.interactionService = interactionService;
    }

    @KafkaListener(topics = "${app.kafka.topic.invoice-events:invoice-events}", groupId = "crm-service")
    public void onInvoiceEvent(InvoiceEvent event) {
        if (event == null || event.getInvoiceId() == null) {
            log.warn("Ignoring invoice event without invoice id");
            return;
        }
        log.debug("Received invoice event {} for invoice {}", event.getType(), event.getInvoiceId());

        // Find contact by customer email
        contactRepository.findByEmail(event.getCustomerEmail()).ifPresent(contact -> {
            interactionService.createFromInvoiceEvent(contact, event);
        });
    }
}