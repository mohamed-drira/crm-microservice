package com.crm.billing.service.impl;

import com.crm.billing.dto.request.InvoiceRequest;
import com.crm.billing.dto.request.InvoiceStatusRequest;
import com.crm.billing.dto.response.InvoiceResponse;
import com.crm.billing.entity.Invoice;
import com.crm.billing.entity.enums.InvoiceStatus;
import com.crm.billing.exception.ApiException;
import com.crm.billing.messaging.InvoiceEvent;
import com.crm.billing.messaging.InvoiceEventProducer;
import com.crm.billing.repository.InvoiceRepository;
import com.crm.billing.service.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceEventProducer eventProducer;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              InvoiceEventProducer eventProducer) {
        this.invoiceRepository = invoiceRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        if (request.getDueDate().isBefore(request.getIssueDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Due date must not be before issue date");
        }

        String invoiceNumber = generateInvoiceNumber();
        if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
            throw new ApiException(HttpStatus.CONFLICT, "Invoice number already exists: " + invoiceNumber);
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomerName(request.getCustomerName());
        invoice.setCustomerEmail(request.getCustomerEmail());
        invoice.setAmount(request.getAmount());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setStatus(InvoiceStatus.DRAFT);

        Invoice saved = invoiceRepository.save(invoice);
        eventProducer.publish(InvoiceEvent.created(saved));
        return InvoiceResponse.from(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse updateStatus(Long id, InvoiceStatusRequest request) {
        Invoice invoice = getEntity(id);
        InvoiceStatus current = invoice.getStatus();
        InvoiceStatus newStatus = request.getStatus();

        boolean allowed = switch (current) {
            case DRAFT -> newStatus == InvoiceStatus.SENT || newStatus == InvoiceStatus.CANCELLED;
            case SENT -> newStatus == InvoiceStatus.PAID || newStatus == InvoiceStatus.CANCELLED || newStatus == InvoiceStatus.OVERDUE;
            case PAID, CANCELLED, OVERDUE -> false;
        };

        if (!allowed) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cannot transition invoice from " + current + " to " + newStatus);
        }

        invoice.setStatus(newStatus);
        Invoice saved = invoiceRepository.save(invoice);
        eventProducer.publish(InvoiceEvent.statusUpdated(saved));
        return InvoiceResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long id) {
        return InvoiceResponse.from(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAll() {
        return invoiceRepository.findAll().stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByCustomerEmail(String customerEmail) {
        return invoiceRepository.findByCustomerEmail(customerEmail).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    private Invoice getEntity(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice not found: " + id));
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}