package com.crm.billing.service;

import com.crm.billing.dto.request.InvoiceRequest;
import com.crm.billing.dto.request.InvoiceStatusRequest;
import com.crm.billing.dto.response.InvoiceResponse;
import com.crm.billing.entity.enums.InvoiceStatus;

import java.util.List;

public interface InvoiceService {

    InvoiceResponse create(InvoiceRequest request);

    InvoiceResponse updateStatus(Long id, InvoiceStatusRequest request);

    InvoiceResponse getById(Long id);

    List<InvoiceResponse> getAll();

    List<InvoiceResponse> getByStatus(InvoiceStatus status);

    List<InvoiceResponse> getByCustomerEmail(String customerEmail);
}