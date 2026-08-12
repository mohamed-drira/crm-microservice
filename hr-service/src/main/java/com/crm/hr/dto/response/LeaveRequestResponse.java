package com.crm.hr.dto.response;

import com.crm.hr.entity.LeaveRequest;
import com.crm.hr.entity.enums.LeaveRequestStatus;
import com.crm.hr.entity.enums.LeaveType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class LeaveRequestResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveRequestStatus status;
    private LocalDateTime createdAt;

    public static LeaveRequestResponse from(LeaveRequest leaveRequest) {
        return LeaveRequestResponse.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployee().getId())
                .employeeName(leaveRequest.getEmployee().getFirstName()
                        + " " + leaveRequest.getEmployee().getLastName())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .createdAt(leaveRequest.getCreatedAt())
                .build();
    }
}
