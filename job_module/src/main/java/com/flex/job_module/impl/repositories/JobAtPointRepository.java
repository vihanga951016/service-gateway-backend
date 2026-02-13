package com.flex.job_module.impl.repositories;

import com.flex.job_module.impl.entities.JobAtPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobAtPointRepository extends JpaRepository<JobAtPoint, Integer> {

    @Query("""
       SELECT j
       FROM JobAtPoint j
       LEFT JOIN j.job js
       WHERE j.servicePoint.id = :id
         AND js.appointmentDate = :date
       ORDER BY j.startTime ASC
       """)
    List<JobAtPoint> findByJobIdAndAppointmentDate(
            @Param("id") Integer id,
            @Param("date") LocalDate date
    );

    List<JobAtPoint> findAllByJobIdAndDummyEntityIsTrue(Integer jobId);
}
