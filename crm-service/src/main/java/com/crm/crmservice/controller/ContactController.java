package com.crm.crmservice.controller;

import com.crm.crmservice.dto.request.ContactRequest;
import com.crm.crmservice.dto.request.ContactStatusRequest;
import com.crm.crmservice.dto.response.ContactResponse;
import com.crm.crmservice.entity.enums.ContactStatus;
import com.crm.crmservice.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm/contacts")
@Tag(name = "Contacts", description = "Contact management")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a contact")
    public ContactResponse create(@Valid @RequestBody ContactRequest request) {
        return contactService.create(request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update contact status")
    public ContactResponse updateStatus(@PathVariable Long id, @Valid @RequestBody ContactStatusRequest request) {
        return contactService.updateStatus(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a contact by id")
    public ContactResponse getById(@PathVariable Long id) {
        return contactService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List all contacts")
    public List<ContactResponse> getAll() {
        return contactService.getAll();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List contacts by status")
    public List<ContactResponse> getByStatus(@PathVariable ContactStatus status) {
        return contactService.getByStatus(status);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "List contacts by employee id")
    public List<ContactResponse> getByEmployeeId(@PathVariable Long employeeId) {
        return contactService.getByEmployeeId(employeeId);
    }

    @GetMapping("/company/{company}")
    @Operation(summary = "List contacts by company")
    public List<ContactResponse> getByCompany(@PathVariable String company) {
        return contactService.getByCompany(company);
    }
}