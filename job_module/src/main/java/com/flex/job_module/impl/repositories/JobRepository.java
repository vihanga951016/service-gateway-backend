package com.flex.job_module.impl.repositories;

import com.flex.job_module.api.http.DTO.ServiceTimeProjection;
import com.flex.job_module.impl.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Integer> {
    Job findByIdAndDummyIsTrue(Integer id);
}
