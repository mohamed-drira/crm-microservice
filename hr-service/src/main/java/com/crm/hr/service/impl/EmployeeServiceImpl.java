package com.crm.hr.service.impl;

import com.crm.hr.dto.request.EmployeeRequest;
import com.crm.hr.dto.response.EmployeeResponse;
import com.crm.hr.entity.Department;
import com.crm.hr.entity.Employee;
import com.crm.hr.exception.ApiException;
import com.crm.hr.messaging.EmployeeEvent;
import com.crm.hr.messaging.EmployeeEventProducer;
import com.crm.hr.repository.DepartmentRepository;
import com.crm.hr.repository.EmployeeRepository;
import com.crm.hr.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeEventProducer eventProducer;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               EmployeeEventProducer eventProducer) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use: " + request.getEmail());
        }

        Employee employee = new Employee();
        apply(employee, request);

        Employee saved = employeeRepository.save(employee);
        eventProducer.publish(toEvent(EmployeeEvent.Type.EMPLOYEE_CREATED, saved));
        return EmployeeResponse.from(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = getEntity(id);
        apply(employee, request);

        Employee saved = employeeRepository.save(employee);
        eventProducer.publish(toEvent(EmployeeEvent.Type.EMPLOYEE_UPDATED, saved));
        return EmployeeResponse.from(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Employee employee = getEntity(id);
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return EmployeeResponse.from(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getByDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Department not found: " + departmentId);
        }
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    private void apply(Employee employee, EmployeeRequest request) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                            "Department not found: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }
    }

    private EmployeeEvent toEvent(EmployeeEvent.Type type, Employee employee) {
        return EmployeeEvent.build(type, employee.getId(), employee.getFirstName(),
                employee.getLastName(), employee.getEmail(),
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                employee.getSalary(),
                employee.getStatus() != null ? employee.getStatus().name() : null);
    }

    private Employee getEntity(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee not found: " + id));
    }
}
