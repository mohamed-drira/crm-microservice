package com.crm.billing.controller;

import com.crm.billing.dto.request.InvoiceRequest;
import com.crm.billing.dto.request.InvoiceStatusRequest;
import com.crm.billing.dto.response.InvoiceResponse;
import com.crm.billing.entity.enums.InvoiceStatus;
import com.crm.billing.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/invoices")
@Tag(name = "Invoices", description = "Invoice management")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an invoice (publishes InvoiceCreated event)")
    public InvoiceResponse create(@Valid @RequestBody InvoiceRequest request) {
        return invoiceService.create(request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update invoice status (publishes InvoiceStatusUpdated event)")
    public InvoiceResponse updateStatus(@PathVariable Long id, @Valid @RequestBody InvoiceStatusRequest request) {
        return invoiceService.updateStatus(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an invoice by id")
    public InvoiceResponse getById(@PathVariable Long id) {
        return invoiceService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List all invoices")
    public List<InvoiceResponse> getAll() {
        return invoiceService.getAll();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List invoices by status")
    public List<InvoiceResponse> getByStatus(@PathVariable InvoiceStatus status) {
        return invoiceService.getByStatus(status);
    }

    @GetMapping("/customer/{customerEmail}")
    @Operation(summary = "List invoices by customer email")
    public List<InvoiceResponse> getByCustomerEmail(@PathVariable String customerEmail) {
        return invoiceService.getByCustomerEmail(customerEmail);
    }
}