package com.crm.hr.controller;

import com.crm.hr.dto.request.EmployeeRequest;
import com.crm.hr.dto.response.EmployeeResponse;
import com.crm.hr.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/employees")
@Tag(name = "Employees", description = "Employee management")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an employee (publishes EmployeeCreated event)")
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee (publishes EmployeeUpdated event)")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an employee")
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee by id")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List all employees")
    public List<EmployeeResponse> getAll() {
        return employeeService.getAll();
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "List employees of a department")
    public List<EmployeeResponse> getByDepartment(@PathVariable Long departmentId) {
        return employeeService.getByDepartment(departmentId);
    }
}
