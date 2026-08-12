package com.crm.payroll.controller;

import com.crm.payroll.dto.response.EmployeeSnapshotResponse;
import com.crm.payroll.service.EmployeeSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payroll/employees")
@Tag(name = "Employee Snapshots", description = "Local read-only employee copies hydrated from employee-events")
public class EmployeeSnapshotController {

    private final EmployeeSnapshotService employeeSnapshotService;

    public EmployeeSnapshotController(EmployeeSnapshotService employeeSnapshotService) {
        this.employeeSnapshotService = employeeSnapshotService;
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "Get the local copy of an employee")
    public EmployeeSnapshotResponse getByEmployeeId(@PathVariable Long employeeId) {
        return employeeSnapshotService.getByEmployeeId(employeeId);
    }

    @GetMapping
    @Operation(summary = "List all local employee copies")
    public List<EmployeeSnapshotResponse> getAll() {
        return employeeSnapshotService.getAll();
    }
}
