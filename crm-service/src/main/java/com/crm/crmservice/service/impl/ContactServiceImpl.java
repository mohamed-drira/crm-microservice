package com.crm.crmservice.service.impl;

import com.crm.crmservice.dto.request.ContactRequest;
import com.crm.crmservice.dto.request.ContactStatusRequest;
import com.crm.crmservice.dto.response.ContactResponse;
import com.crm.crmservice.entity.Contact;
import com.crm.crmservice.entity.enums.ContactStatus;
import com.crm.crmservice.exception.ApiException;
import com.crm.crmservice.messaging.EmployeeEvent;
import com.crm.crmservice.repository.ContactRepository;
import com.crm.crmservice.service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional
    public ContactResponse create(ContactRequest request) {
        if (contactRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Contact with email already exists: " + request.getEmail());
        }

        Contact contact = new Contact();
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setCompany(request.getCompany());
        contact.setPosition(request.getPosition());
        contact.setStatus(request.getStatus());
        contact.setEmployeeId(request.getEmployeeId());
        contact.setEmployeeName(request.getEmployeeName());

        return ContactResponse.from(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public ContactResponse updateStatus(Long id, ContactStatusRequest request) {
        Contact contact = getEntity(id);
        contact.setStatus(request.getStatus());
        return ContactResponse.from(contactRepository.save(contact));
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getById(Long id) {
        return ContactResponse.from(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getAll() {
        return contactRepository.findAll().stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getByStatus(ContactStatus status) {
        return contactRepository.findByStatus(status).stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getByEmployeeId(Long employeeId) {
        return contactRepository.findByEmployeeId(employeeId).stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getByCompany(String company) {
        return contactRepository.findByCompany(company).stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void createFromEmployeeEvent(EmployeeEvent event) {
        if (contactRepository.existsByEmail(event.getEmail())) {
            log.debug("Contact already exists for employee {}", event.getEmployeeId());
            return;
        }

        Contact contact = new Contact();
        contact.setFirstName(event.getFirstName());
        contact.setLastName(event.getLastName());
        contact.setEmail(event.getEmail());
        contact.setCompany(event.getDepartment());
        contact.setPosition("Employee");
        contact.setStatus(ContactStatus.CUSTOMER);
        contact.setEmployeeId(event.getEmployeeId());
        contact.setEmployeeName(event.getFirstName() + " " + event.getLastName());

        contactRepository.save(contact);
        log.info("Created contact from employee event for employee {}", event.getEmployeeId());
    }

    @Override
    @Transactional
    public void updateFromEmployeeEvent(EmployeeEvent event) {
        contactRepository.findByEmail(event.getEmail()).ifPresent(contact -> {
            contact.setFirstName(event.getFirstName());
            contact.setLastName(event.getLastName());
            contact.setCompany(event.getDepartment());
            contact.setEmployeeName(event.getFirstName() + " " + event.getLastName());
            contactRepository.save(contact);
            log.info("Updated contact from employee event for employee {}", event.getEmployeeId());
        });
    }

    private Contact getEntity(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Contact not found: " + id));
    }
}