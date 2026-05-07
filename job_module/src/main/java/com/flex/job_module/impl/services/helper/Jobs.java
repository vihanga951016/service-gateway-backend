package com.flex.job_module.impl.services.helper;

import com.flex.job_module.constants.JobStatus;
import com.flex.job_module.constants.JobTypes;
import com.flex.job_module.impl.entities.Customer;
import com.flex.job_module.impl.entities.Job;
import com.flex.job_module.impl.repositories.JobRepository;
import com.flex.service_module.impl.entities.ServiceCenter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class Jobs {

    private final JobRepository jobRepository;

    public Job createAndSave(Customer customer, ServiceCenter serviceCenter, LocalDate appointmentDate, String notes) {
        Job job = Job.builder()
                .customer(customer)
                .serviceCenter(serviceCenter)
                .appointmentDate(appointmentDate)
                .status(JobStatus.PENDING)
                .jobType(JobTypes.WEB)
                .description(notes)
                .createdDate(LocalDate.now())
                .createdTime(LocalTime.now())
                .dummy(true)
                .build();

        return jobRepository.save(job);
    }
}
