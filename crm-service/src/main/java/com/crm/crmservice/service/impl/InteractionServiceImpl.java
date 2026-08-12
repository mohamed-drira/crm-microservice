package com.crm.crmservice.service.impl;

import com.crm.crmservice.dto.request.InteractionRequest;
import com.crm.crmservice.dto.response.InteractionResponse;
import com.crm.crmservice.entity.Contact;
import com.crm.crmservice.entity.Interaction;
import com.crm.crmservice.entity.enums.InteractionType;
import com.crm.crmservice.exception.ApiException;
import com.crm.crmservice.messaging.InvoiceEvent;
import com.crm.crmservice.repository.ContactRepository;
import com.crm.crmservice.repository.InteractionRepository;
import com.crm.crmservice.service.InteractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InteractionServiceImpl implements InteractionService {

    private static final Logger log = LoggerFactory.getLogger(InteractionServiceImpl.class);

    private final InteractionRepository interactionRepository;
    private final ContactRepository contactRepository;

    public InteractionServiceImpl(InteractionRepository interactionRepository,
                                  ContactRepository contactRepository) {
        this.interactionRepository = interactionRepository;
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional
    public InteractionResponse create(InteractionRequest request) {
        Contact contact = contactRepository.findById(request.getContactId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Contact not found: " + request.getContactId()));

        Interaction interaction = new Interaction();
        interaction.setContact(contact);
        interaction.setInteractionType(request.getInteractionType());
        interaction.setDescription(request.getDescription());
        interaction.setInteractionDate(request.getInteractionDate());

        return InteractionResponse.from(interactionRepository.save(interaction));
    }

    @Override
    @Transactional(readOnly = true)
    public InteractionResponse getById(Long id) {
        return InteractionResponse.from(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InteractionResponse> getByContactId(Long contactId) {
        return interactionRepository.findByContactIdOrderByInteractionDateDesc(contactId).stream()
                .map(InteractionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InteractionResponse> getByContactIdAndType(Long contactId, InteractionType type) {
        return interactionRepository.findByContactIdAndInteractionType(contactId, type).stream()
                .map(InteractionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InteractionResponse> getByContactIdAndDateRange(Long contactId, LocalDateTime start, LocalDateTime end) {
        return interactionRepository.findByContactIdAndInteractionDateBetween(contactId, start, end).stream()
                .map(InteractionResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void createFromInvoiceEvent(Contact contact, InvoiceEvent event) {
        Interaction interaction = new Interaction();
        interaction.setContact(contact);
        interaction.setInteractionType(InteractionType.NOTE);
        interaction.setDescription("Invoice " + event.getInvoiceNumber() + " created for " + event.getCustomerName()
                + " (amount: " + event.getAmount() + ", status: " + event.getStatus() + ")");
        interaction.setInteractionDate(event.getTimestamp());

        interactionRepository.save(interaction);
        log.info("Created interaction from invoice event for contact {} and invoice {}", contact.getId(), event.getInvoiceId());
    }

    private Interaction getEntity(Long id) {
        return interactionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Interaction not found: " + id));
    }
}