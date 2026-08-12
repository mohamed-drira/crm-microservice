package com.crm.crmservice.controller;

import com.crm.crmservice.dto.request.InteractionRequest;
import com.crm.crmservice.dto.response.InteractionResponse;
import com.crm.crmservice.entity.enums.InteractionType;
import com.crm.crmservice.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/crm/interactions")
@Tag(name = "Interactions", description = "Interaction management")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an interaction")
    public InteractionResponse create(@Valid @RequestBody InteractionRequest request) {
        return interactionService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an interaction by id")
    public InteractionResponse getById(@PathVariable Long id) {
        return interactionService.getById(id);
    }

    @GetMapping("/contact/{contactId}")
    @Operation(summary = "List interactions for a contact")
    public List<InteractionResponse> getByContactId(@PathVariable Long contactId) {
        return interactionService.getByContactId(contactId);
    }

    @GetMapping("/contact/{contactId}/type/{type}")
    @Operation(summary = "List interactions for a contact by type")
    public List<InteractionResponse> getByContactIdAndType(@PathVariable Long contactId, @PathVariable InteractionType type) {
        return interactionService.getByContactIdAndType(contactId, type);
    }

    @GetMapping("/contact/{contactId}/date-range")
    @Operation(summary = "List interactions for a contact within a date range")
    public List<InteractionResponse> getByContactIdAndDateRange(
            @PathVariable Long contactId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return interactionService.getByContactIdAndDateRange(contactId, start, end);
    }
}