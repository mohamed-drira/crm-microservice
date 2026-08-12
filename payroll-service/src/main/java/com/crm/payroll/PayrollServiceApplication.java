package com.crm.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class PayrollServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayrollServiceApplication.class, args);
    }
}
