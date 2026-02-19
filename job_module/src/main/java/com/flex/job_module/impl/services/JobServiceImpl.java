package com.flex.job_module.impl.services;

import com.flex.job_module.api.http.requests.PrepareJob;
import com.flex.job_module.api.http.responses.PreparedJob;
import com.flex.job_module.api.services.JobService;
import com.flex.job_module.constants.JobStatus;
import com.flex.job_module.impl.entities.Customer;
import com.flex.job_module.impl.entities.Job;
import com.flex.job_module.impl.entities.JobAtPoint;
import com.flex.job_module.impl.repositories.CustomerRepository;
import com.flex.job_module.impl.repositories.JobAtPointRepository;
import com.flex.job_module.impl.repositories.JobRepository;
import com.flex.job_module.impl.services.helper.JobServiceHelper;
import com.flex.service_module.api.http.DTO.BestServicePointForJob;
import com.flex.service_module.impl.entities.AvailableService;
import com.flex.service_module.impl.entities.ServiceCenter;
import com.flex.service_module.impl.entities.ServicePoint;
import com.flex.service_module.impl.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.flex.common_module.http.ReturnResponse.*;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/11/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class JobServiceImpl implements JobService {

    private final ServiceCenterRepository serviceCenterRepository;
    private final CenterClusterRepository centerClusterRepository;
    private final CCSRepository ccsRepository;
    private final AvailableServiceRepository availableServiceRepository;
    private final CustomerRepository customerRepository;

    private final JobAtPointRepository jobAtPointRepository;
    private final JobRepository jobRepository;
    private final ServicePointRepository servicePointRepository;

    private final JobServiceHelper jobServiceHelper;

    @Transactional
    @Override
    public ResponseEntity<?> prepareJob(PrepareJob prepareJob, HttpServletRequest request) {

        ServiceCenter serviceCenter = serviceCenterRepository.findByIdAndDeletedIsFalse(prepareJob.getServiceCenterId());

        if (serviceCenter == null) {
            return CONFLICT("Service center not found");
        }

        List<ServicePoint> servicePointList = servicePointRepository.servicePointsByCenter(serviceCenter.getId());

        if (servicePointList.isEmpty()) {
            return CONFLICT("Service points not found");
        }

        Customer customer = customerRepository.findByPhone(prepareJob.getPhone());

        if (customer == null) {
            customer = Customer.builder()
                    .customer(prepareJob.getCustomer())
                    .phone(prepareJob.getPhone())
                    .dummy(true)
                    .build();
        }

        customerRepository.save(customer);

        Job job = Job.builder()
                .customer(customer)
                .appointmentDate(prepareJob.getAppointmentDate())
                .status(JobStatus.PENDING)
                .dummy(true)
                .build();

        jobRepository.save(job);

        if (prepareJob.getCenterClusterId() != null) {

            if (!centerClusterRepository.existsById(prepareJob.getCenterClusterId())) {
                jobRepository.delete(job);
                customerRepository.delete(customer);
                return CONFLICT("Cluster not found");
            }

            // find services from center cluster
            List<com.flex.service_module.impl.entities.Service> centerClusterServices = ccsRepository
                    .getServicesByCenterClusterId(prepareJob.getCenterClusterId());

            if (centerClusterServices.isEmpty()) {
                jobRepository.delete(job);
                customerRepository.delete(customer);
                return CONFLICT("Services not found for cluster");
            }

            List<JobAtPoint> jobsAtPoint = new ArrayList<>();
            LocalTime startTime = null;
            LocalTime nextStartTime = serviceCenter.getOpenTime();
            // loop services and check create the job at point
            for (com.flex.service_module.impl.entities.Service centerClusterService : centerClusterServices) {
                long minimumServiceTimeFromSec = 0;
                ServicePoint suitablePoint = servicePointList.getFirst();
                LocalTime minimumEndTime = null;

                int i = 0;
                for (ServicePoint servicePoint : servicePointList) {
                    //must have service in service point
                    AvailableService availableService = availableServiceRepository
                            .availableService(centerClusterService.getId(), servicePoint.getId());
                    //if have service in service point
                    if (availableService != null) {
                        //check the previous job is related to the current job.
                        List<JobAtPoint> previousJobs = jobAtPointRepository
                                .findByJobIdAndAppointmentDate(servicePoint.getId(), prepareJob.getAppointmentDate());

                        if (!previousJobs.isEmpty()) {
                            //get previous job ids
                            List<Integer> prevJobIds = previousJobs.stream().map(
                                    j -> j.getJob().getId()
                            ).toList();

                            if (prevJobIds.contains(job.getId())) {

                                nextStartTime = previousJobs.getLast().getEndTime();

                                JobAtPoint createJobAtPoint = jobServiceHelper
                                        .createJobAtPoint(servicePoint, centerClusterService, job,
                                                nextStartTime, true);

                                if (createJobAtPoint != null) {
                                    jobAtPointRepository.save(createJobAtPoint);
                                } else {
                                    jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                                    return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                                }

                                jobsAtPoint.add(createJobAtPoint);
                                break;
                            } else {
                                //find the total service time of all jobs.
                                long totalSeconds = previousJobs.stream()
                                        .mapToLong(j ->
                                                Duration.between(j.getStartTime(), j.getEndTime()).getSeconds()
                                        )
                                        .sum();

                                if (totalSeconds < minimumServiceTimeFromSec) {
                                    minimumServiceTimeFromSec = totalSeconds;
                                    suitablePoint = servicePoint;
                                    minimumEndTime = previousJobs.getLast().getEndTime();
                                    startTime = minimumEndTime;
                                    nextStartTime = minimumEndTime;
                                }
                            }
                        } else {

                            JobAtPoint createJobAtPoint = jobServiceHelper
                                    .createJobAtPoint(servicePoint, centerClusterService, job,
                                            nextStartTime, true);

                            if (createJobAtPoint != null) {
                                jobAtPointRepository.save(createJobAtPoint);
                            } else {
                                jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                                return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                            }

                            nextStartTime = jobServiceHelper.calculateEndTime(nextStartTime,
                                    centerClusterService.getServiceTime(), servicePoint.getCloseTime());

                            jobAtPointRepository.save(createJobAtPoint);

                            jobsAtPoint.add(createJobAtPoint);
                            break;
                        }
                    }
                }

                i++;
                if (servicePointList.size() == (i + 1)) {
                    // 0 means no service points
                    if (minimumServiceTimeFromSec == 0) {
                        return CONFLICT("No suitable service point for " + centerClusterService.getName());
                    }

                    JobAtPoint createJobAtPoint = jobServiceHelper
                            .createJobAtPoint(suitablePoint, centerClusterService, job,
                                    nextStartTime, true);

                    if (createJobAtPoint != null) {
                        jobAtPointRepository.save(createJobAtPoint);
                    } else {
                        jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                        return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                    }

                    jobAtPointRepository.save(createJobAtPoint);

                    jobsAtPoint.add(createJobAtPoint);
                }

            }
            //cluster completed
            return DATA(PreparedJob.builder()
                    .jobId(job.getId())
                    .customerId(customer.getId())
                    .appointmentDate(prepareJob.getAppointmentDate().toString())
                    .appointmentTime(startTime != null ? startTime.toString() : null)
                    .jobsAtPoint(jobsAtPoint).build());

        } else {

            if (prepareJob.getServicesIds().isEmpty()) {
                return CONFLICT("Choose services first");
            }

            Integer bestServicePoint = servicePointList.getFirst().getId();
            int availableServices = 0;
            double minimumServiceTimeFromSec = 0;

            HashMap<Integer, Integer> availablePointsForService = new HashMap<>();

            for (Integer serviceId : prepareJob.getServicesIds()) {
                // todo: find the available service points by service and service center

                // todo: put service id as key and point ids as values in hashmap.
            }

            return null;
        }
    }

    @Override
    public ResponseEntity<?> removeDummyJob(Integer jobId, Integer customerId, HttpServletRequest request) {
        log.info(request.getRequestURI());

        Customer customer = customerRepository.findByIdAndDummyIsTrue(customerId);

        if (customer == null) {
            return CONFLICT("Customer not found");
        }

        Job job = jobRepository.findByIdAndDummyIsTrue(jobId);

        if (job == null) {
            return CONFLICT("Job not found");
        }

        List<JobAtPoint> jobsAtPoint = jobAtPointRepository.findAllByJobIdAndDummyEntityIsTrue(jobId);

        if (!jobsAtPoint.isEmpty()) {
            jobAtPointRepository.deleteAll(jobsAtPoint);
        }

        jobRepository.delete(job);
        jobAtPointRepository.flush();

        customerRepository.delete(customer);
        customerRepository.flush();

        return SUCCESS("");
    }
}
