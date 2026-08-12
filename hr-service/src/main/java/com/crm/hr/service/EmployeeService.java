package com.crm.hr.service;

import com.crm.hr.dto.request.EmployeeRequest;
import com.crm.hr.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse create(EmployeeRequest request);

    EmployeeResponse update(Long id, EmployeeRequest request);

    void delete(Long id);

    EmployeeResponse getById(Long id);

    List<EmployeeResponse> getAll();

    List<EmployeeResponse> getByDepartment(Long departmentId);
}
