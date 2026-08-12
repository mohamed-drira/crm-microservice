package com.crm.payroll.dto.response;

import com.crm.payroll.entity.EmployeeSnapshot;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class EmployeeSnapshotResponse {

    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private BigDecimal salary;
    private String status;
    private LocalDateTime updatedAt;

    public static EmployeeSnapshotResponse from(EmployeeSnapshot snapshot) {
        return EmployeeSnapshotResponse.builder()
                .employeeId(snapshot.getEmployeeId())
                .firstName(snapshot.getFirstName())
                .lastName(snapshot.getLastName())
                .email(snapshot.getEmail())
                .department(snapshot.getDepartment())
                .salary(snapshot.getSalary())
                .status(snapshot.getStatus() != null ? snapshot.getStatus().name() : null)
                .updatedAt(snapshot.getUpdatedAt())
                .build();
    }
}
