package com.crm.payroll.controller;

import com.crm.payroll.dto.request.FeeRequest;
import com.crm.payroll.dto.request.PayrollRequest;
import com.crm.payroll.dto.request.PayrollStatusRequest;
import com.crm.payroll.dto.response.PayrollResponse;
import com.crm.payroll.entity.enums.PayrollStatus;
import com.crm.payroll.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@Tag(name = "Payroll", description = "Payroll runs and fees")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/payrolls")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate a payroll for an employee and period (publishes PayrollCreated)")
    public PayrollResponse generate(@Valid @RequestBody PayrollRequest request) {
        return payrollService.generate(request);
    }

    @PatchMapping("/payrolls/{id}/status")
    @Operation(summary = "Change payroll status (publishes PayrollUpdated)")
    public PayrollResponse updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody PayrollStatusRequest request) {
        return payrollService.updateStatus(id, request.getStatus());
    }

    @PostMapping("/payrolls/{id}/fees")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a fee to a DRAFT payroll")
    public PayrollResponse addFee(@PathVariable Long id, @Valid @RequestBody FeeRequest request) {
        return payrollService.addFee(id, request);
    }

    @DeleteMapping("/payrolls/{payrollId}/fees/{feeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a fee from a DRAFT payroll")
    public void removeFee(@PathVariable Long payrollId, @PathVariable Long feeId) {
        payrollService.removeFee(payrollId, feeId);
    }

    @GetMapping("/payrolls/{id}")
    @Operation(summary = "Get a payroll by id")
    public PayrollResponse getById(@PathVariable Long id) {
        return payrollService.getById(id);
    }

    @GetMapping("/payrolls")
    @Operation(summary = "List payrolls (optionally filtered by status or employee)")
    public List<PayrollResponse> getAll(@RequestParam(required = false) String status,
                                        @RequestParam(required = false) Long employeeId) {
        if (employeeId != null) {
            return payrollService.getByEmployee(employeeId);
        }
        if (status != null) {
            PayrollStatus payrollStatus;
            try {
                payrollStatus = PayrollStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status: " + status + ". Allowed values: DRAFT, PAID, CANCELLED");
            }
            return payrollService.getByStatus(payrollStatus);
        }
        return payrollService.getAll();
    }
}
