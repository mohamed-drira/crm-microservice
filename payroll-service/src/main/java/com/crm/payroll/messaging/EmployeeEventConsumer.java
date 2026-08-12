package com.crm.payroll.messaging;

import com.crm.payroll.service.EmployeeSnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmployeeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEventConsumer.class);

    private final EmployeeSnapshotService employeeSnapshotService;

    public EmployeeEventConsumer(EmployeeSnapshotService employeeSnapshotService) {
        this.employeeSnapshotService = employeeSnapshotService;
    }

    @KafkaListener(topics = "${app.kafka.topic.employee-events:employee-events}", groupId = "payroll-service")
    public void onEmployeeEvent(EmployeeEvent event) {
        if (event == null || event.getEmployeeId() == null) {
            log.warn("Ignoring employee event without employee id");
            return;
        }
        log.debug("Received employee event {} for employee {}", event.getType(), event.getEmployeeId());
        employeeSnapshotService.upsert(event);
    }
}
