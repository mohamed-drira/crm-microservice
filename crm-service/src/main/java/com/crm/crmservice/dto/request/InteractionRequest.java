package com.crm.crmservice.dto.request;

import com.crm.crmservice.entity.enums.InteractionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InteractionRequest {

    @NotNull
    private Long contactId;

    @NotNull
    private InteractionType interactionType;

    @NotBlank
    private String description;

    @NotNull
    private LocalDateTime interactionDate;
}