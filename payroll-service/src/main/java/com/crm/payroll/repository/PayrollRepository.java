package com.crm.payroll.repository;

import com.crm.payroll.entity.Payroll;
import com.crm.payroll.entity.enums.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByEmployeeId(Long employeeId);

    List<Payroll> findByStatus(PayrollStatus status);

    Optional<Payroll> findByEmployeeIdAndStatusAndPeriodStartAndPeriodEnd(
            Long employeeId, PayrollStatus status, LocalDate periodStart, LocalDate periodEnd);
}
