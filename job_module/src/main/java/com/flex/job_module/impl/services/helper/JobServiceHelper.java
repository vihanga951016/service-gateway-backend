package com.flex.job_module.impl.services.helper;

import com.flex.common_module.constants.Colors;
import com.flex.job_module.constants.JobStatus;
import com.flex.job_module.impl.entities.Job;
import com.flex.job_module.impl.entities.JobAtPoint;
import com.flex.service_module.impl.entities.Service;
import com.flex.service_module.impl.entities.ServicePoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/12/2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class JobServiceHelper {

    public JobAtPoint createJobAtPoint(ServicePoint servicePoint, Service service, Job job, LocalTime startTime, boolean dummy) {


        // calculate end time
        // todo: some times we have to add a rest time for this
        LocalTime calculatedEndTime = calculateEndTime(startTime, service.getServiceTime());

        //create and add job at point
        return JobAtPoint.builder()
                .servicePoint(servicePoint)
                .service(service)
                .job(job)
                .startTime(startTime) //start time is the end time of last job
                .endTime(calculatedEndTime)
                .status(JobStatus.PENDING)
                .dummyEntity(dummy)
                .build();
    }

    public LocalTime calculateEndTime(LocalTime startTime, Date serviceTime) {
        // convert service time(Date) to LocalDate
        LocalTime serviceDuration =
                ((java.sql.Time) serviceTime).toLocalTime();

        return startTime
                .plusHours(serviceDuration.getHour())
                .plusMinutes(serviceDuration.getMinute())
                .plusSeconds(serviceDuration.getSecond());
    }

}
