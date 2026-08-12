package com.crm.hr.controller;

import com.crm.hr.dto.request.LeaveRequestRequest;
import com.crm.hr.dto.request.LeaveRequestStatusRequest;
import com.crm.hr.dto.response.LeaveRequestResponse;
import com.crm.hr.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/leave-requests")
@Tag(name = "Leave Requests", description = "Leave request management")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a leave request")
    public LeaveRequestResponse create(@Valid @RequestBody LeaveRequestRequest request) {
        return leaveRequestService.create(request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Approve, reject or cancel a leave request")
    public LeaveRequestResponse updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody LeaveRequestStatusRequest request) {
        return leaveRequestService.updateStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a leave request")
    public void delete(@PathVariable Long id) {
        leaveRequestService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a leave request by id")
    public LeaveRequestResponse getById(@PathVariable Long id) {
        return leaveRequestService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List all leave requests")
    public List<LeaveRequestResponse> getAll() {
        return leaveRequestService.getAll();
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "List leave requests of an employee")
    public List<LeaveRequestResponse> getByEmployee(@PathVariable Long employeeId) {
        return leaveRequestService.getByEmployee(employeeId);
    }
}
