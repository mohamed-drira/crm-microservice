package com.crm.hr.dto.request;

import com.crm.hr.entity.enums.LeaveRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveRequestStatusRequest {

    @NotNull
    private LeaveRequestStatus status;
}
