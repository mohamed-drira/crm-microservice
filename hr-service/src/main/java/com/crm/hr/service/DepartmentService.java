package com.crm.hr.service;

import com.crm.hr.dto.request.DepartmentRequest;
import com.crm.hr.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentRequest request);

    void delete(Long id);

    DepartmentResponse getById(Long id);

    List<DepartmentResponse> getAll();
}
