package com.crm.payroll.service.impl;

import com.crm.payroll.dto.request.FeeRequest;
import com.crm.payroll.dto.request.PayrollRequest;
import com.crm.payroll.dto.response.PayrollResponse;
import com.crm.payroll.entity.EmployeeSnapshot;
import com.crm.payroll.entity.Fee;
import com.crm.payroll.entity.Payroll;
import com.crm.payroll.entity.enums.FeeType;
import com.crm.payroll.entity.enums.PayrollStatus;
import com.crm.payroll.exception.ApiException;
import com.crm.payroll.messaging.PayrollEvent;
import com.crm.payroll.messaging.PayrollEventProducer;
import com.crm.payroll.repository.EmployeeSnapshotRepository;
import com.crm.payroll.repository.FeeRepository;
import com.crm.payroll.repository.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private PayrollRepository payrollRepository;
    @Mock
    private FeeRepository feeRepository;
    @Mock
    private EmployeeSnapshotRepository snapshotRepository;
    @Mock
    private PayrollEventProducer eventProducer;

    private PayrollServiceImpl payrollService;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollServiceImpl(payrollRepository, feeRepository, snapshotRepository, eventProducer);
    }

    private PayrollRequest request() {
        PayrollRequest request = new PayrollRequest();
        request.setEmployeeId(1L);
        request.setPeriodStart(LocalDate.of(2026, 3, 1));
        request.setPeriodEnd(LocalDate.of(2026, 3, 31));
        return request;
    }

    private EmployeeSnapshot snapshot() {
        EmployeeSnapshot snapshot = new EmployeeSnapshot();
        snapshot.setEmployeeId(1L);
        snapshot.setFirstName("Jane");
        snapshot.setLastName("Smith");
        snapshot.setSalary(new BigDecimal("1000.00"));
        return snapshot;
    }

    @Test
    void generateCreatesDraftAndPublishesEvent() {
        when(snapshotRepository.findByEmployeeId(1L)).thenReturn(Optional.of(snapshot()));
        when(payrollRepository.findByEmployeeIdAndStatusAndPeriodStartAndPeriodEnd(
                anyLong(), any(), any(), any())).thenReturn(Optional.empty());
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(inv -> {
            Payroll p = inv.getArgument(0);
            p.setId(3L);
            return p;
        });

        PayrollResponse response = payrollService.generate(request());

        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getNetAmount()).isEqualByComparingTo("1000.00");
        assertThat(response.getEmployeeName()).isEqualTo("Jane Smith");

        ArgumentCaptor<PayrollEvent> captor = ArgumentCaptor.forClass(PayrollEvent.class);
        verify(eventProducer, times(1)).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(PayrollEvent.Type.PAYROLL_CREATED);
        assertThat(captor.getValue().getPayrollId()).isEqualTo(3L);
    }

    @Test
    void generateRejectsMissingSnapshot() {
        when(snapshotRepository.findByEmployeeId(99L)).thenReturn(Optional.empty());

        PayrollRequest request = request();
        request.setEmployeeId(99L);

        assertThatThrownBy(() -> payrollService.generate(request))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void generateRejectsDuplicateDraft() {
        when(snapshotRepository.findByEmployeeId(1L)).thenReturn(Optional.of(snapshot()));
        when(payrollRepository.findByEmployeeIdAndStatusAndPeriodStartAndPeriodEnd(
                anyLong(), any(), any(), any())).thenReturn(Optional.of(new Payroll()));

        assertThatThrownBy(() -> payrollService.generate(request()))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateStatusMovesDraftToPaid() {
        Payroll payroll = new Payroll();
        payroll.setId(3L);
        payroll.setStatus(PayrollStatus.DRAFT);
        when(payrollRepository.findById(3L)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(inv -> inv.getArgument(0));

        PayrollResponse response = payrollService.updateStatus(3L, PayrollStatus.PAID);

        assertThat(response.getStatus()).isEqualTo("PAID");
        verify(eventProducer, times(1)).publish(any(PayrollEvent.class));
    }

    @Test
    void updateStatusRejectsPaidToCancelled() {
        Payroll payroll = new Payroll();
        payroll.setId(3L);
        payroll.setStatus(PayrollStatus.PAID);
        when(payrollRepository.findById(3L)).thenReturn(Optional.of(payroll));

        assertThatThrownBy(() -> payrollService.updateStatus(3L, PayrollStatus.CANCELLED))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addFeeRecalculatesNet() {
        Payroll payroll = new Payroll();
        payroll.setId(3L);
        payroll.setBaseSalary(new BigDecimal("1000.00"));
        payroll.setNetAmount(new BigDecimal("1000.00"));
        payroll.setStatus(PayrollStatus.DRAFT);
        when(payrollRepository.findById(3L)).thenReturn(Optional.of(payroll));
        when(feeRepository.save(any(Fee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(inv -> inv.getArgument(0));

        FeeRequest fee = new FeeRequest();
        fee.setFeeType(FeeType.BONUS);
        fee.setAmount(new BigDecimal("250.00"));
        fee.setDescription("Quarterly bonus");

        PayrollResponse response = payrollService.addFee(3L, fee);

        assertThat(response.getBonuses()).isEqualByComparingTo("250.00");
        assertThat(response.getDeductions()).isEqualByComparingTo("0.00");
        assertThat(response.getNetAmount()).isEqualByComparingTo("1250.00");
    }

    @Test
    void addFeeRejectedWhenNotDraft() {
        Payroll payroll = new Payroll();
        payroll.setId(3L);
        payroll.setStatus(PayrollStatus.PAID);
        when(payrollRepository.findById(3L)).thenReturn(Optional.of(payroll));

        FeeRequest fee = new FeeRequest();
        fee.setFeeType(FeeType.DEDUCTION);
        fee.setAmount(new BigDecimal("10"));

        assertThatThrownBy(() -> payrollService.addFee(3L, fee))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }
}
