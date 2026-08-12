package com.crm.crmservice.messaging;

import com.crm.crmservice.entity.Contact;
import com.crm.crmservice.entity.enums.ContactStatus;
import com.crm.crmservice.repository.ContactRepository;
import com.crm.crmservice.service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmployeeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEventConsumer.class);

    private final ContactService contactService;
    private final ContactRepository contactRepository;

    public EmployeeEventConsumer(ContactService contactService,
                                 ContactRepository contactRepository) {
        this.contactService = contactService;
        this.contactRepository = contactRepository;
    }

    @KafkaListener(topics = "${app.kafka.topic.employee-events:employee-events}", groupId = "crm-service")
    public void onEmployeeEvent(EmployeeEvent event) {
        if (event == null || event.getEmployeeId() == null) {
            log.warn("Ignoring employee event without employee id");
            return;
        }
        log.debug("Received employee event {} for employee {}", event.getType(), event.getEmployeeId());

        switch (event.getType()) {
            case EMPLOYEE_CREATED -> contactService.createFromEmployeeEvent(event);
            case EMPLOYEE_UPDATED -> contactService.updateFromEmployeeEvent(event);
        }
    }
}