package com.crm.notification.messaging;

import com.crm.notification.entity.Notification;
import com.crm.notification.entity.enums.NotificationType;
import com.crm.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final String adminUsernames;

    public NotificationEventConsumer(NotificationRepository notificationRepository,
                                     @Value("${app.notification.admin-usernames:admin}") String adminUsernames) {
        this.notificationRepository = notificationRepository;
        this.adminUsernames = adminUsernames;
    }

    @KafkaListener(topics = "${app.kafka.topic.employee-events:employee-events}",
            groupId = "notification-service")
    public void onEmployeeEvent(EmployeeEvent event) {
        if (event == null || event.employeeId() == null) return;

        String title;
        String message;
        NotificationType type;

        if (event.type() == EmployeeEvent.Type.EMPLOYEE_CREATED) {
            type = NotificationType.EMPLOYEE_CREATED;
            title = "New Employee Added";
            message = String.format("Employee %s %s has been added to department %s.",
                    event.firstName(), event.lastName(),
                    event.department() != null ? event.department() : "N/A");
        } else {
            type = NotificationType.EMPLOYEE_UPDATED;
            title = "Employee Updated";
            message = String.format("Employee %s %s details have been updated.",
                    event.firstName(), event.lastName());
        }

        saveNotification(event.eventId(), type, title, message, event.employeeId());
    }

    @KafkaListener(topics = "${app.kafka.topic.payroll-events:payroll-events}",
            groupId = "notification-service")
    public void onPayrollEvent(PayrollEvent event) {
        if (event == null || event.payrollId() == null) return;

        String title;
        String message;
        NotificationType type;

        if (event.type() == PayrollEvent.Type.PAYROLL_CREATED) {
            type = NotificationType.PAYROLL_CREATED;
            title = "Payroll Generated";
            message = String.format("Payroll for %s (period %s to %s) has been generated. Net: %s",
                    event.employeeName(), event.periodStart(), event.periodEnd(), event.netAmount());
        } else {
            type = NotificationType.PAYROLL_UPDATED;
            title = "Payroll Updated";
            message = String.format("Payroll for %s status changed to %s.",
                    event.employeeName(), event.status());
        }

        saveNotification(event.eventId(), type, title, message, event.payrollId());
    }

    @KafkaListener(topics = "${app.kafka.topic.invoice-events:invoice-events}",
            groupId = "notification-service")
    public void onInvoiceEvent(InvoiceEvent event) {
        if (event == null || event.invoiceId() == null) return;

        String title;
        String message;
        NotificationType type;

        if (event.type() == InvoiceEvent.Type.INVOICE_CREATED) {
            type = NotificationType.INVOICE_CREATED;
            title = "Invoice Created";
            message = String.format("Invoice %s for %s (%s) - Amount: %s",
                    event.invoiceNumber(), event.customerName(), event.customerEmail(), event.amount());
        } else {
            type = NotificationType.INVOICE_STATUS_UPDATED;
            title = "Invoice Status Updated";
            message = String.format("Invoice %s status changed to %s.",
                    event.invoiceNumber(), event.status());
        }

        saveNotification(event.eventId(), type, title, message, event.invoiceId());
    }

    private void saveNotification(UUID eventId, NotificationType type, String title, String message, Long referenceId) {
        if (notificationRepository.existsByEventId(eventId)) {
            log.debug("Skipping duplicate notification for event {}", eventId);
            return;
        }

        for (String admin : adminUsernames.split(",")) {
            Notification notification = new Notification();
            notification.setEventId(eventId);
            notification.setRecipientUsername(admin.trim());
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setReferenceId(referenceId);
            try {
                notificationRepository.save(notification);
            } catch (DataIntegrityViolationException e) {
                log.debug("Skipping duplicate notification for event {} (race condition caught by DB constraint)", eventId);
            }
        }
    }
}
