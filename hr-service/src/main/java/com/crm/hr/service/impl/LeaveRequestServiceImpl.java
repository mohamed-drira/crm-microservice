package com.crm.hr.service.impl;

import com.crm.hr.dto.request.LeaveRequestRequest;
import com.crm.hr.dto.response.LeaveRequestResponse;
import com.crm.hr.entity.Employee;
import com.crm.hr.entity.LeaveRequest;
import com.crm.hr.entity.enums.LeaveRequestStatus;
import com.crm.hr.exception.ApiException;
import com.crm.hr.repository.EmployeeRepository;
import com.crm.hr.repository.LeaveRequestRepository;
import com.crm.hr.service.LeaveRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository,
                                   EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public LeaveRequestResponse create(LeaveRequestRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End date must not be before start date");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Employee not found: " + request.getEmployeeId()));

        if (hasDateOverlap(employee.getId(), request.getStartDate(), request.getEndDate())) {
            throw new ApiException(HttpStatus.CONFLICT, "Leave request overlaps an existing request");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(request.getLeaveType());
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(request.getReason());
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        return LeaveRequestResponse.from(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @Transactional
    public LeaveRequestResponse updateStatus(Long id, LeaveRequestStatus status) {
        LeaveRequest leaveRequest = getEntity(id);
        if (status == leaveRequest.getStatus()) {
            return LeaveRequestResponse.from(leaveRequest);
        }
        leaveRequest.setStatus(status);
        return LeaveRequestResponse.from(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LeaveRequest leaveRequest = getEntity(id);
        if (leaveRequest.getStatus() == LeaveRequestStatus.APPROVED) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete an approved leave request");
        }
        leaveRequestRepository.delete(leaveRequest);
    }

    @Override
    public LeaveRequestResponse getById(Long id) {
        return LeaveRequestResponse.from(getEntity(id));
    }

    @Override
    public List<LeaveRequestResponse> getAll() {
        return leaveRequestRepository.findAll().stream()
                .map(LeaveRequestResponse::from)
                .toList();
    }

    @Override
    public List<LeaveRequestResponse> getByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Employee not found: " + employeeId);
        }
        return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .map(LeaveRequestResponse::from)
                .toList();
    }

    private boolean hasDateOverlap(Long employeeId, LocalDate start, LocalDate end) {
        return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .filter(r -> r.getStatus() == LeaveRequestStatus.PENDING
                        || r.getStatus() == LeaveRequestStatus.APPROVED)
                .anyMatch(r -> !start.isAfter(r.getEndDate()) && !end.isBefore(r.getStartDate()));
    }

    private LeaveRequest getEntity(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Leave request not found: " + id));
    }
}
