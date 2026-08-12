package com.crm.crmservice.repository;

import com.crm.crmservice.entity.Contact;
import com.crm.crmservice.entity.enums.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findByEmail(String email);

    List<Contact> findByStatus(ContactStatus status);

    List<Contact> findByEmployeeId(Long employeeId);

    List<Contact> findByCompany(String company);

    boolean existsByEmail(String email);
}