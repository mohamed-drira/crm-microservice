package com.crm.hr.repository;

import com.crm.hr.entity.LeaveRequest;
import com.crm.hr.entity.enums.LeaveRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByStatus(LeaveRequestStatus status);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequestStatus status);
}
