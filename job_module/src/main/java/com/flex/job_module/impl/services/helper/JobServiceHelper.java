package com.flex.job_module.impl.services.helper;

import com.flex.common_module.constants.Colors;
import com.flex.job_module.constants.JobStatus;
import com.flex.job_module.impl.entities.Customer;
import com.flex.job_module.impl.entities.Job;
import com.flex.job_module.impl.entities.JobAtPoint;
import com.flex.job_module.impl.repositories.CustomerRepository;
import com.flex.job_module.impl.repositories.JobAtPointRepository;
import com.flex.job_module.impl.repositories.JobRepository;
import com.flex.service_module.impl.entities.Service;
import com.flex.service_module.impl.entities.ServicePoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static com.flex.common_module.http.ReturnResponse.CONFLICT;

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

    private final CustomerRepository customerRepository;
    private final JobRepository jobRepository;
    private final JobAtPointRepository jobAtPointRepository;

    public JobAtPoint createJobAtPoint(ServicePoint servicePoint, Service service, Job job, LocalTime startTime, boolean dummy) {
        // calculate end time
        // todo: some times we have to add a rest time for this
        LocalTime calculatedEndTime = calculateEndTime(startTime, service.getServiceTime(), servicePoint.getCloseTime());

        if (calculatedEndTime != null) {
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
        } else {
            return null;
        }


    }

    public LocalTime calculateEndTime(LocalTime startTime, Date serviceTime, LocalTime closeTime) {
        // convert service time(Date) to LocalDate
        LocalTime serviceDuration =
                ((java.sql.Time) serviceTime).toLocalTime();

        LocalTime endTime = startTime
                .plusHours(serviceDuration.getHour())
                .plusMinutes(serviceDuration.getMinute())
                .plusSeconds(serviceDuration.getSecond());

        if (closeTime.isAfter(endTime)) {
            return startTime
                    .plusHours(serviceDuration.getHour())
                    .plusMinutes(serviceDuration.getMinute())
                    .plusSeconds(serviceDuration.getSecond());
        } else {
            return null;
        }
    }

    public void clearDummyData(Integer customerId, Integer jobId) {
        Customer customer = customerRepository.findByIdAndDummyIsTrue(customerId);

        Job job = jobRepository.findByIdAndDummyIsTrue(jobId);

        List<JobAtPoint> jobsAtPoint = jobAtPointRepository.findAllByJobIdAndDummyEntityIsTrue(jobId);

        if (!jobsAtPoint.isEmpty()) {
            jobAtPointRepository.deleteAll(jobsAtPoint);
        }

        if (job != null) {
            jobRepository.delete(job);
            jobAtPointRepository.flush();
        }

        if (customer != null) {
            customerRepository.delete(customer);
            customerRepository.flush();
        }
    }

}
