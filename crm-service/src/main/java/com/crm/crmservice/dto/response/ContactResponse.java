package com.crm.crmservice.dto.response;

import com.crm.crmservice.entity.Contact;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ContactResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String company;
    private String position;
    private String status;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ContactResponse from(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .company(contact.getCompany())
                .position(contact.getPosition())
                .status(contact.getStatus().name())
                .employeeId(contact.getEmployeeId())
                .employeeName(contact.getEmployeeName())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}