package com.crm.hr.dto.response;

import com.crm.hr.entity.Employee;
import com.crm.hr.entity.enums.EmployeeStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime createdAt;

    public static EmployeeResponse from(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .createdAt(employee.getCreatedAt())
                .build();
    }
}
