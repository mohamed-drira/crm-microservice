package com.crm.payroll.repository;

import com.crm.payroll.entity.EmployeeSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeSnapshotRepository extends JpaRepository<EmployeeSnapshot, Long> {

    Optional<EmployeeSnapshot> findByEmployeeId(Long employeeId);
}
