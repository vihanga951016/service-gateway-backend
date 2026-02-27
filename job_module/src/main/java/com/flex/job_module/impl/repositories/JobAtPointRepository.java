package com.flex.job_module.impl.repositories;

import com.flex.job_module.api.http.DTO.JobTimelineProjection;
import com.flex.job_module.api.http.DTO.MinimumServiceTimePoint;
import com.flex.job_module.impl.entities.JobAtPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobAtPointRepository extends JpaRepository<JobAtPoint, Integer> {

    boolean existsByServicePointIdAndStatusIsLessThan(Integer servicePointId, Integer status);

    @Query("""
       SELECT j
       FROM JobAtPoint j
       LEFT JOIN j.job js
       WHERE j.servicePoint.id = :id
         AND js.appointmentDate = :date
       ORDER BY j.startTime ASC
       """)
    List<JobAtPoint> findByJobIdAndAppointmentDate(
            @Param("id") Integer servicePointId,
            @Param("date") LocalDate appointmentDate
    );

    List<JobAtPoint> findAllByJobIdAndDummyEntityIsTrue(Integer jobId);

    @Query(value = """
        SELECT sp.id AS servicePointId,
               IFNULL(
                   SEC_TO_TIME(
                       SUM(TIME_TO_SEC(jp.end_time) - TIME_TO_SEC(jp.start_time))
                   ),
                   '00:00:00'
               ) AS totalServiceTime
        FROM service_point sp
        LEFT JOIN jobs_at_point jp ON jp.service_point_id = sp.id
        WHERE sp.id IN (:points)
        GROUP BY sp.id
        ORDER BY totalServiceTime LIMIT 1""", nativeQuery = true)
    MinimumServiceTimePoint findServicePointWithMinTotalServiceTime(
            @Param("points") List<Integer> points);

    @Query(value = """
        SELECT jp.id AS jobAtPointId,
            j.id AS jobId,
            c.customer AS customerName,
            s.name AS serviceName,
            CASE WHEN jp.status = 0 THEN 'pending'
                WHEN jp.status = 1 THEN 'serving'
                ELSE 'completed'
            END AS status,
            jp.start_time AS startTime,
            jp.end_time AS endTime,
            jp.service_point_id as pointId,
            CONCAT(
                DATE_FORMAT(jp.start_time, '%h:%i:%s'),
                ' - ',
                DATE_FORMAT(jp.end_time, '%h:%i:%s')
            ) AS fromTo
        FROM jobs_at_point jp
        LEFT JOIN jobs j ON jp.job_id = j.id
        LEFT JOIN customers c ON c.id = j.customer_id
        LEFT JOIN services s ON s.id = jp.service_id
        WHERE jp.service_point_id = :servicePointId
          AND j.appointment_date = :appointmentDate
        ORDER BY jp.service_point_id, jp.start_time
        """,
            nativeQuery = true)
    List<JobTimelineProjection> getJobTimeline(
            @Param("servicePointId") Integer servicePointId,
            @Param("appointmentDate") LocalDate appointmentDate
    );
}
