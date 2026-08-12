package com.crm.payroll.service;

import com.crm.payroll.dto.request.FeeRequest;
import com.crm.payroll.dto.request.PayrollRequest;
import com.crm.payroll.dto.response.PayrollResponse;
import com.crm.payroll.entity.enums.PayrollStatus;

import java.util.List;

public interface PayrollService {

    PayrollResponse generate(PayrollRequest request);

    PayrollResponse updateStatus(Long id, PayrollStatus status);

    PayrollResponse addFee(Long payrollId, FeeRequest request);

    PayrollResponse removeFee(Long payrollId, Long feeId);

    PayrollResponse getById(Long id);

    List<PayrollResponse> getAll();

    List<PayrollResponse> getByEmployee(Long employeeId);

    List<PayrollResponse> getByStatus(PayrollStatus status);
}
