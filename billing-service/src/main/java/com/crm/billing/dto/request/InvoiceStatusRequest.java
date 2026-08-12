package com.crm.billing.dto.request;

import com.crm.billing.entity.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceStatusRequest {

    @NotNull
    private InvoiceStatus status;
}
