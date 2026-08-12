package com.crm.billing.repository;

import com.crm.billing.entity.Invoice;
import com.crm.billing.entity.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByCustomerEmail(String customerEmail);

    boolean existsByInvoiceNumber(String invoiceNumber);
}
