package com.flex.job_module.impl.repositories;

import com.flex.job_module.impl.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Integer> {
    Job findByIdAndDummyIsTrue(Integer id);

    Job getJobById(Integer id);

    @Query("SELECT j FROM Job j WHERE j.customer.id=:id AND j.status < 2")
    Job jobForCustomer(@Param("id") Integer customerId);

}
