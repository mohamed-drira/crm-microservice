package com.crm.hr.dto.request;

import com.crm.hr.entity.enums.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveRequestRequest {

    @NotNull
    @Positive
    private Long employeeId;

    @NotNull
    private LeaveType leaveType;

    @NotNull
    @FutureOrPresent
    private LocalDate startDate;

    @NotNull
    @FutureOrPresent
    private LocalDate endDate;

    @Size(max = 255)
    private String reason;
}
