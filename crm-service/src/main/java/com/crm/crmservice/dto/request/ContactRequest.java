package com.crm.crmservice.dto.request;

import com.crm.crmservice.entity.enums.ContactStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    private String phone;

    private String company;

    private String position;

    @NotNull
    private ContactStatus status = ContactStatus.LEAD;

    private Long employeeId;

    private String employeeName;
}