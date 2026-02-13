package com.flex.job_module.impl.repositories;

import com.flex.job_module.impl.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Customer findByPhone(String phone);

    Customer findByIdAndDummyIsTrue(Integer id);
}
