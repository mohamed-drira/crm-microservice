package com.crm.payroll.dto.response;

import com.crm.payroll.entity.Fee;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class FeeResponse {

    private Long id;
    private String feeType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;

    public static FeeResponse from(Fee fee) {
        return FeeResponse.builder()
                .id(fee.getId())
                .feeType(fee.getFeeType().name())
                .amount(fee.getAmount())
                .description(fee.getDescription())
                .createdAt(fee.getCreatedAt())
                .build();
    }
}
