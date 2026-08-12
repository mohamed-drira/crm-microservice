package com.crm.hr.service;

import com.crm.hr.dto.request.LeaveRequestRequest;
import com.crm.hr.dto.response.LeaveRequestResponse;

import java.util.List;

public interface LeaveRequestService {

    LeaveRequestResponse create(LeaveRequestRequest request);

    LeaveRequestResponse updateStatus(Long id, com.crm.hr.entity.enums.LeaveRequestStatus status);

    void delete(Long id);

    LeaveRequestResponse getById(Long id);

    List<LeaveRequestResponse> getAll();

    List<LeaveRequestResponse> getByEmployee(Long employeeId);
}
