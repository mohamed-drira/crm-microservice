package com.crm.hr.repository;

import com.crm.hr.entity.Employee;
import com.crm.hr.entity.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByStatus(EmployeeStatus status);

    boolean existsByEmail(String email);
}
