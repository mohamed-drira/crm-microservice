package com.crm.payroll.repository;

import com.crm.payroll.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, Long> {

    List<Fee> findByPayrollId(Long payrollId);
}
