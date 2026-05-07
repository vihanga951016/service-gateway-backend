package com.flex.job_module.impl.services.helper;

import com.flex.job_module.impl.entities.Customer;
import com.flex.job_module.impl.entities.Job;
import com.flex.job_module.impl.repositories.CustomerRepository;
import com.flex.job_module.impl.repositories.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DummyEntities {

    private final JobRepository jobRepository;
    private final CustomerRepository customerRepository;

    public void cleanDummies (Job job, Customer customer) {
        jobRepository.delete(job);
        customerRepository.delete(customer);
    }
}
