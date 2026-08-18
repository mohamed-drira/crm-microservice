package com.crm.hr.service.impl;

import com.crm.hr.dto.request.DepartmentRequest;
import com.crm.hr.dto.response.DepartmentResponse;
import com.crm.hr.entity.Department;
import com.crm.hr.exception.ApiException;
import com.crm.hr.repository.DepartmentRepository;
import com.crm.hr.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new ApiException(HttpStatus.CONFLICT, "Department already exists: " + request.getName());
        }
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getEntity(id);
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = getEntity(id);
        if (!department.getEmployees().isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot delete department with " + department.getEmployees().size() + " employees");
        }
        departmentRepository.delete(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return DepartmentResponse.from(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    private Department getEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Department not found: " + id));
    }
}
