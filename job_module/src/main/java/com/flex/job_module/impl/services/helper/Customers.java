package com.flex.job_module.impl.services.helper;

import com.flex.job_module.impl.entities.Customer;
import com.flex.job_module.impl.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Customers {

    private final CustomerRepository customerRepository;

    public Customer findOrCreateCustomer(String phone, String name) {
        Customer customer = customerRepository.findByPhone(phone);

        if (customer == null) {
            customer = Customer.builder()
                    .customer(name)
                    .phone(phone)
                    .dummy(true)
                    .build();
        }

        return customer;
    }
}
