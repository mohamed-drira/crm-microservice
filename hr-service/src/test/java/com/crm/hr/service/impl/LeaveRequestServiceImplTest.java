package com.crm.hr.service.impl;

import com.crm.hr.dto.request.LeaveRequestRequest;
import com.crm.hr.dto.response.LeaveRequestResponse;
import com.crm.hr.entity.Employee;
import com.crm.hr.entity.LeaveRequest;
import com.crm.hr.entity.enums.LeaveRequestStatus;
import com.crm.hr.entity.enums.LeaveType;
import com.crm.hr.exception.ApiException;
import com.crm.hr.repository.EmployeeRepository;
import com.crm.hr.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    private LeaveRequestServiceImpl leaveRequestService;

    @BeforeEach
    void setUp() {
        leaveRequestService = new LeaveRequestServiceImpl(leaveRequestRepository, employeeRepository);
    }

    private LeaveRequestRequest request() {
        LeaveRequestRequest request = new LeaveRequestRequest();
        request.setEmployeeId(1L);
        request.setLeaveType(LeaveType.ANNUAL);
        request.setStartDate(LocalDate.of(2026, 2, 10));
        request.setEndDate(LocalDate.of(2026, 2, 12));
        return request;
    }

    @Test
    void createSavesPendingLeaveRequest() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(List.of());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> {
            LeaveRequest lr = inv.getArgument(0);
            lr.setId(9L);
            return lr;
        });

        LeaveRequestResponse response = leaveRequestService.create(request());

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getStatus()).isEqualTo(LeaveRequestStatus.PENDING);
        assertThat(response.getLeaveType()).isEqualTo(LeaveType.ANNUAL);
        assertThat(response.getEmployeeName()).isEqualTo("Jane Smith");
    }

    @Test
    void createRejectsEndDateBeforeStartDate() {
        LeaveRequestRequest request = request();
        request.setStartDate(LocalDate.of(2026, 2, 12));
        request.setEndDate(LocalDate.of(2026, 2, 10));

        assertThatThrownBy(() -> leaveRequestService.create(request))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createRejectsOverlappingRequest() {
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        LeaveRequest existing = new LeaveRequest();
        existing.setEmployee(employee);
        existing.setStartDate(LocalDate.of(2026, 2, 11));
        existing.setEndDate(LocalDate.of(2026, 2, 15));
        existing.setStatus(LeaveRequestStatus.APPROVED);
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> leaveRequestService.create(request()))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateStatusChangesStatus() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Jane");
        employee.setLastName("Smith");

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(9L);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
        when(leaveRequestRepository.findById(9L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestResponse response = leaveRequestService.updateStatus(9L, LeaveRequestStatus.APPROVED);

        assertThat(response.getStatus()).isEqualTo(LeaveRequestStatus.APPROVED);
    }
}
