package com.crm.hr.dto.response;

import com.crm.hr.entity.Department;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentResponse {

    private Long id;
    private String name;
    private String description;
    private long employeeCount;

    public static DepartmentResponse from(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .employeeCount(department.getEmployees().size())
                .build();
    }
}
