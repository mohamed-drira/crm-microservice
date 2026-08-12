package com.crm.hr.controller;

import com.crm.hr.dto.request.DepartmentRequest;
import com.crm.hr.dto.response.DepartmentResponse;
import com.crm.hr.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/departments")
@Tag(name = "Departments", description = "Department management")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a department")
    public DepartmentResponse create(@Valid @RequestBody DepartmentRequest request) {
        return departmentService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a department")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return departmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a department")
    public void delete(@PathVariable Long id) {
        departmentService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a department by id")
    public DepartmentResponse getById(@PathVariable Long id) {
        return departmentService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List all departments")
    public List<DepartmentResponse> getAll() {
        return departmentService.getAll();
    }
}
