package com.crm.hr.service.impl;

import com.crm.hr.dto.request.EmployeeRequest;
import com.crm.hr.dto.response.EmployeeResponse;
import com.crm.hr.entity.Employee;
import com.crm.hr.exception.ApiException;
import com.crm.hr.messaging.EmployeeEvent;
import com.crm.hr.messaging.EmployeeEventProducer;
import com.crm.hr.repository.DepartmentRepository;
import com.crm.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private EmployeeEventProducer eventProducer;

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, departmentRepository, eventProducer);
    }

    private EmployeeRequest request(String email) {
        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail(email);
        request.setPosition("Engineer");
        return request;
    }

    @Test
    void createPublishesEmployeeCreatedEvent() {
        when(employeeRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(5L);
            return e;
        });

        EmployeeResponse response = employeeService.create(request("jane@example.com"));

        assertThat(response.getId()).isEqualTo(5L);

        ArgumentCaptor<EmployeeEvent> captor = ArgumentCaptor.forClass(EmployeeEvent.class);
        verify(eventProducer, times(1)).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EmployeeEvent.Type.EMPLOYEE_CREATED);
        assertThat(captor.getValue().getEmployeeId()).isEqualTo(5L);
        assertThat(captor.getValue().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(employeeRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request("jane@example.com")))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        verifyNoInteractions(eventProducer);
    }

    @Test
    void updatePublishesEmployeeUpdatedEvent() {
        Employee employee = new Employee();
        employee.setId(5L);
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setEmail("jane@example.com");
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        employeeService.update(5L, request("jane@example.com"));

        ArgumentCaptor<EmployeeEvent> captor = ArgumentCaptor.forClass(EmployeeEvent.class);
        verify(eventProducer, times(1)).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EmployeeEvent.Type.EMPLOYEE_UPDATED);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(99L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }
}
