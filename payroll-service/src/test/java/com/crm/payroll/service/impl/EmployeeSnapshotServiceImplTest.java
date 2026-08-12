package com.crm.payroll.service.impl;

import com.crm.payroll.entity.EmployeeSnapshot;
import com.crm.payroll.entity.enums.EmployeeStatus;
import com.crm.payroll.messaging.EmployeeEvent;
import com.crm.payroll.repository.EmployeeSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeSnapshotServiceImplTest {

    @Mock
    private EmployeeSnapshotRepository snapshotRepository;

    private EmployeeSnapshotServiceImpl snapshotService;

    @BeforeEach
    void setUp() {
        snapshotService = new EmployeeSnapshotServiceImpl(snapshotRepository);
    }

    private EmployeeEvent event() {
        EmployeeEvent event = new EmployeeEvent();
        event.setType(EmployeeEvent.Type.EMPLOYEE_CREATED);
        event.setEmployeeId(7L);
        event.setFirstName("Jane");
        event.setLastName("Smith");
        event.setEmail("jane@example.com");
        event.setDepartment("Engineering");
        event.setSalary(new BigDecimal("5000.00"));
        event.setStatus("ON_LEAVE");
        return event;
    }

    @Test
    void upsertCreatesSnapshotWhenMissing() {
        when(snapshotRepository.findByEmployeeId(7L)).thenReturn(Optional.empty());
        when(snapshotRepository.save(any(EmployeeSnapshot.class))).thenAnswer(inv -> inv.getArgument(0));

        snapshotService.upsert(event());

        ArgumentCaptor<EmployeeSnapshot> captor = ArgumentCaptor.forClass(EmployeeSnapshot.class);
        verify(snapshotRepository, times(1)).save(captor.capture());
        EmployeeSnapshot saved = captor.getValue();
        assertThat(saved.getEmployeeId()).isEqualTo(7L);
        assertThat(saved.getDepartment()).isEqualTo("Engineering");
        assertThat(saved.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    void upsertUpdatesExistingSnapshot() {
        EmployeeSnapshot existing = new EmployeeSnapshot();
        existing.setId(1L);
        existing.setEmployeeId(7L);
        existing.setFirstName("Old");
        when(snapshotRepository.findByEmployeeId(7L)).thenReturn(Optional.of(existing));
        when(snapshotRepository.save(any(EmployeeSnapshot.class))).thenAnswer(inv -> inv.getArgument(0));

        snapshotService.upsert(event());

        assertThat(existing.getFirstName()).isEqualTo("Jane");
        assertThat(existing.getEmployeeId()).isEqualTo(7L);
    }
}
