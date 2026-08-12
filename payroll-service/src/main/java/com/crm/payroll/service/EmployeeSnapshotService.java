package com.crm.payroll.service;

import com.crm.payroll.dto.response.EmployeeSnapshotResponse;
import com.crm.payroll.messaging.EmployeeEvent;

import java.util.List;

public interface EmployeeSnapshotService {

    void upsert(EmployeeEvent event);

    EmployeeSnapshotResponse getByEmployeeId(Long employeeId);

    List<EmployeeSnapshotResponse> getAll();
}
