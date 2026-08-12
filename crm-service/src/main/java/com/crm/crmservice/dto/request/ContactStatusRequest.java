package com.crm.crmservice.dto.request;

import com.crm.crmservice.entity.enums.ContactStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactStatusRequest {

    @NotNull
    private ContactStatus status;
}