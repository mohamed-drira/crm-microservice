package com.crm.crmservice.service;

import com.crm.crmservice.dto.request.InteractionRequest;
import com.crm.crmservice.dto.response.InteractionResponse;
import com.crm.crmservice.entity.enums.InteractionType;
import com.crm.crmservice.messaging.InvoiceEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface InteractionService {

    InteractionResponse create(InteractionRequest request);

    InteractionResponse getById(Long id);

    List<InteractionResponse> getByContactId(Long contactId);

    List<InteractionResponse> getByContactIdAndType(Long contactId, InteractionType type);

    List<InteractionResponse> getByContactIdAndDateRange(Long contactId, LocalDateTime start, LocalDateTime end);

    void createFromInvoiceEvent(com.crm.crmservice.entity.Contact contact, InvoiceEvent event);
}