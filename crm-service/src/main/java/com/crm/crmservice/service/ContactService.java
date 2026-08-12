package com.crm.crmservice.service;

import com.crm.crmservice.dto.request.ContactRequest;
import com.crm.crmservice.dto.request.ContactStatusRequest;
import com.crm.crmservice.dto.response.ContactResponse;
import com.crm.crmservice.entity.enums.ContactStatus;
import com.crm.crmservice.messaging.EmployeeEvent;

import java.util.List;

public interface ContactService {

    ContactResponse create(ContactRequest request);

    ContactResponse updateStatus(Long id, ContactStatusRequest request);

    ContactResponse getById(Long id);

    List<ContactResponse> getAll();

    List<ContactResponse> getByStatus(ContactStatus status);

    List<ContactResponse> getByEmployeeId(Long employeeId);

    List<ContactResponse> getByCompany(String company);

    void createFromEmployeeEvent(EmployeeEvent event);

    void updateFromEmployeeEvent(EmployeeEvent event);
}