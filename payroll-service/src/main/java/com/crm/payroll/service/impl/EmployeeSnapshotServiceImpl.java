package com.crm.payroll.service.impl;

import com.crm.payroll.dto.response.EmployeeSnapshotResponse;
import com.crm.payroll.entity.EmployeeSnapshot;
import com.crm.payroll.entity.enums.EmployeeStatus;
import com.crm.payroll.exception.ApiException;
import com.crm.payroll.messaging.EmployeeEvent;
import com.crm.payroll.repository.EmployeeSnapshotRepository;
import com.crm.payroll.service.EmployeeSnapshotService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeSnapshotServiceImpl implements EmployeeSnapshotService {

    private final EmployeeSnapshotRepository snapshotRepository;

    public EmployeeSnapshotServiceImpl(EmployeeSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    @Transactional
    public void upsert(EmployeeEvent event) {
        EmployeeSnapshot snapshot = snapshotRepository.findByEmployeeId(event.getEmployeeId())
                .orElseGet(EmployeeSnapshot::new);

        snapshot.setEmployeeId(event.getEmployeeId());
        snapshot.setFirstName(event.getFirstName());
        snapshot.setLastName(event.getLastName());
        snapshot.setEmail(event.getEmail());
        snapshot.setDepartment(event.getDepartment());
        snapshot.setSalary(event.getSalary());
        snapshot.setStatus(parseStatus(event));
        snapshot.setUpdatedAt(LocalDateTime.now());

        snapshotRepository.save(snapshot);
    }

    private EmployeeStatus parseStatus(EmployeeEvent event) {
        if (event.getStatus() == null) {
            return null;
        }
        return switch (event.getStatus().toLowerCase()) {
            case "on_leave" -> EmployeeStatus.ON_LEAVE;
            case "inactive" -> EmployeeStatus.INACTIVE;
            default -> EmployeeStatus.ACTIVE;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeSnapshotResponse getByEmployeeId(Long employeeId) {
        return EmployeeSnapshotResponse.from(getEntity(employeeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSnapshotResponse> getAll() {
        return snapshotRepository.findAll().stream()
                .map(EmployeeSnapshotResponse::from)
                .toList();
    }

    private EmployeeSnapshot getEntity(Long employeeId) {
        return snapshotRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "No employee snapshot for employee " + employeeId
                                + " (employee events may not have arrived yet)"));
    }
}
