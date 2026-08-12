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
import com.crm.payroll.service.PayrollService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Service
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final FeeRepository feeRepository;
    private final EmployeeSnapshotRepository snapshotRepository;
    private final PayrollEventProducer eventProducer;

    public PayrollServiceImpl(PayrollRepository payrollRepository,
                              FeeRepository feeRepository,
                              EmployeeSnapshotRepository snapshotRepository,
                              PayrollEventProducer eventProducer) {
        this.payrollRepository = payrollRepository;
        this.feeRepository = feeRepository;
        this.snapshotRepository = snapshotRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    @Transactional
    public PayrollResponse generate(PayrollRequest request) {
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Period end must not be before period start");
        }

        EmployeeSnapshot snapshot = snapshotRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "No employee snapshot for employee " + request.getEmployeeId()
                                + " (employee events may not have arrived yet)"));

        if (payrollRepository
                .findByEmployeeIdAndStatusAndPeriodStartAndPeriodEnd(
                        request.getEmployeeId(), PayrollStatus.DRAFT,
                        request.getPeriodStart(), request.getPeriodEnd())
                .isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "A DRAFT payroll already exists for this employee and period");
        }

        Payroll payroll = new Payroll();
        payroll.setEmployeeId(snapshot.getEmployeeId());
        payroll.setEmployeeName(snapshot.getFirstName() + " " + snapshot.getLastName());
        payroll.setPeriodStart(request.getPeriodStart());
        payroll.setPeriodEnd(request.getPeriodEnd());
        payroll.setBaseSalary(snapshot.getSalary());
        payroll.setNetAmount(snapshot.getSalary());
        payroll.setStatus(PayrollStatus.DRAFT);

        Payroll saved = payrollRepository.save(payroll);
        eventProducer.publish(PayrollEvent.created(PayrollEvent.PayrollSnapshot.from(saved)));
        return PayrollResponse.from(saved);
    }

    @Override
    @Transactional
    public PayrollResponse updateStatus(Long id, PayrollStatus newStatus) {
        Payroll payroll = getEntity(id);
        PayrollStatus current = payroll.getStatus();

        boolean allowed = switch (current) {
            case DRAFT -> newStatus == PayrollStatus.PAID || newStatus == PayrollStatus.CANCELLED;
            case PAID, CANCELLED -> false;
        };
        if (!allowed) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cannot transition payroll from " + current + " to " + newStatus);
        }

        payroll.setStatus(newStatus);
        Payroll saved = payrollRepository.save(payroll);
        eventProducer.publish(PayrollEvent.updated(PayrollEvent.PayrollSnapshot.from(saved)));
        return PayrollResponse.from(saved);
    }

    @Override
    @Transactional
    public PayrollResponse addFee(Long payrollId, FeeRequest request) {
        Payroll payroll = getEntity(payrollId);
        requireDraft(payroll);

        Fee fee = new Fee();
        fee.setPayroll(payroll);
        fee.setFeeType(request.getFeeType());
        fee.setAmount(request.getAmount());
        fee.setDescription(request.getDescription());

        payroll.getFees().add(fee);
        feeRepository.save(fee);
        recalculate(payroll);
        return PayrollResponse.from(payrollRepository.save(payroll));
    }

    @Override
    @Transactional
    public PayrollResponse removeFee(Long payrollId, Long feeId) {
        Payroll payroll = getEntity(payrollId);
        requireDraft(payroll);

        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Fee not found: " + feeId));
        if (!fee.getPayroll().getId().equals(payrollId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Fee does not belong to payroll " + payrollId);
        }

        payroll.getFees().remove(fee);
        feeRepository.delete(fee);
        recalculate(payroll);
        return PayrollResponse.from(payrollRepository.save(payroll));
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollResponse getById(Long id) {
        return PayrollResponse.from(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getAll() {
        return payrollRepository.findAll().stream()
                .map(PayrollResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getByEmployee(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId).stream()
                .map(PayrollResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getByStatus(PayrollStatus status) {
        return payrollRepository.findByStatus(status).stream()
                .map(PayrollResponse::from)
                .toList();
    }

    private void requireDraft(Payroll payroll) {
        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new ApiException(HttpStatus.CONFLICT, "Payroll is " + payroll.getStatus() + " and can no longer be modified");
        }
    }

    private void recalculate(Payroll payroll) {
        BigDecimal bonuses = BigDecimal.ZERO;
        BigDecimal deductions = BigDecimal.ZERO;
        for (Fee fee : payroll.getFees()) {
            if (isBonus(fee.getFeeType())) {
                bonuses = bonuses.add(fee.getAmount());
            } else {
                deductions = deductions.add(fee.getAmount());
            }
        }
        payroll.setBonuses(bonuses);
        payroll.setDeductions(deductions);
        payroll.setNetAmount(payroll.getBaseSalary().add(bonuses).subtract(deductions));
    }

    private boolean isBonus(FeeType feeType) {
        return EnumSet.of(FeeType.BONUS, FeeType.ALLOWANCE).contains(feeType);
    }

    private Payroll getEntity(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payroll not found: " + id));
    }
}
