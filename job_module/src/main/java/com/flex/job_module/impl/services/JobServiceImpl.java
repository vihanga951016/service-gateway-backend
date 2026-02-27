package com.flex.job_module.impl.services;

import com.flex.common_module.constants.Colors;
import com.flex.job_module.api.http.DTO.JobTimelineProjection;
import com.flex.job_module.api.http.DTO.MinimumServiceTimePoint;
import com.flex.job_module.api.http.requests.PointJobs;
import com.flex.job_module.api.http.requests.PrepareJob;
import com.flex.job_module.api.http.responses.JobsSchedule;
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

import java.sql.Date;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

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
    private final ServicesRepository servicesRepository;

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

        Job hasJobForThisCustomer = jobRepository
                .findByCustomer_IdAndAndAppointmentDate(customer.getId(), prepareJob.getAppointmentDate());

        if (hasJobForThisCustomer != null) {
            return CONFLICT("Already have job for this customer");
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

            Set<JobAtPoint> jobsAtPoint = new TreeSet<>(
                    Comparator.comparing(JobAtPoint::getStartTime)
            );
            
            // this represents the last created job end time
            // use to define the next job's start time
            LocalTime nextStartTime = serviceCenter.getOpenTime();

            // this will save all next start times. using for get the appointment time
            List<LocalTime> allStartTimes = new ArrayList<>();

            // this take as all service points are empty at the start, this is using to address the nextStartTime issue
            // for empty service points.
            // * if the service point has no jobs, the nextStartTime must be equal to the last created jobs end time or
            // *
            List<Integer> emptyServicePoints =
                    new ArrayList<>(
                            servicePointList.stream()
                                    .map(ServicePoint::getId)
                                    .toList()
                    );

            // loop services and check create the job at point
            for (com.flex.service_module.impl.entities.Service centerClusterService : centerClusterServices) {
                long minimumServiceTimeFromSec = 86400;
                ServicePoint suitablePoint = servicePointList.getFirst();
                LocalTime minimumEndTime = null;

                // this is using for avoid create other jobs after breaking the loop. check the usage
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
                                allStartTimes.add(nextStartTime);

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

                                    boolean hasEmptyPoints = false;

                                    if (!emptyServicePoints.isEmpty()) {
                                        List<Integer> assignPointsForThisService = availableServiceRepository
                                                .pointsByService(centerClusterService.getId());

                                        for (Integer id: assignPointsForThisService) {
                                            if (!jobAtPointRepository
                                                    .existsByServicePointIdAndStatusIsLessThan(id,
                                                            JobStatus.COMPLETED)) {
                                                hasEmptyPoints = true;
                                                break;
                                            } else {
                                                emptyServicePoints.remove(id);
                                            }
                                        }
                                    }

                                    if (nextStartTime.isBefore(minimumEndTime) && !hasEmptyPoints) {
                                        nextStartTime = minimumEndTime;
                                        allStartTimes.add(nextStartTime);
                                    }
                                }
                            } // this point has jobs, but no sub jobs for this creating job
                        } else {
                            allStartTimes.add(nextStartTime);

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
                            allStartTimes.add(nextStartTime);

                            jobAtPointRepository.save(createJobAtPoint);

                            jobsAtPoint.add(createJobAtPoint);
                            break;
                        } // no jobs in this service point
                    } // this service has not available in this point
                    i = i + 1;
                } // go to the next service point

                // if statement is protecting form unnecessary jobs after broke the loop.
                if (servicePointList.size() == i) {

                    // 0 means no service points
                    if (minimumServiceTimeFromSec == 0) {
                        jobServiceHelper.clearDummyData(customer.getId(), job.getId());
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

                    nextStartTime = createJobAtPoint.getEndTime();
                    allStartTimes.add(nextStartTime);
                    jobAtPointRepository.save(createJobAtPoint);

                    jobsAtPoint.add(createJobAtPoint);
                }
            }

            LocalTime appointmentTime = null;
            if (!allStartTimes.isEmpty()) {
                appointmentTime = allStartTimes.stream().min(LocalTime::compareTo).get();
            }

            //cluster completed
            return DATA(PreparedJob.builder()
                    .jobId(job.getId())
                    .customerId(customer.getId())
                    .appointmentDate(prepareJob.getAppointmentDate().toString())
                    .appointmentTime(appointmentTime != null ? appointmentTime.toString() : null)
                    .jobsAtPoint(jobsAtPoint).build());

        } else {

            if (prepareJob.getServicesIds().isEmpty()) {
                jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                return CONFLICT("Choose services first");
            }

            // custom services
            List<com.flex.service_module.impl.entities.Service> services = servicesRepository
                    .getServicesByIds(prepareJob.getServicesIds());

            // find all services available service points
            List<ServicePoint> points = servicePointRepository
                    .findServicePointsHavingAllServices(prepareJob.getServicesIds(),
                            prepareJob.getServicesIds().size());

            Set<JobAtPoint> jobsAtPoint = new TreeSet<>(
                    Comparator.comparing(JobAtPoint::getStartTime)
            );

            if (!points.isEmpty()) {
                log.info("{} 1 {}",Colors.YELLOW,Colors.RESET);
                List<Integer> pointIds = points.stream().map(ServicePoint::getId).toList();

                // find the service point which has the lowest service time
                MinimumServiceTimePoint lowestServiceTimePoint = jobAtPointRepository.findServicePointWithMinTotalServiceTime(pointIds);

                // get the previous jobs of the point which has minimum service time
                List<JobAtPoint> previousJobs = jobAtPointRepository
                        .findByJobIdAndAppointmentDate(lowestServiceTimePoint.getServicePointId(), prepareJob.getAppointmentDate());

                // create the entity from the point which has minimum service time
                ServicePoint servicePoint = servicePointRepository
                        .findByIdAndDeletedIsFalse(lowestServiceTimePoint.getServicePointId());

                // calculate the total service time for custom services
                long totalMillis = services.stream()
                        .filter(s -> s.getServiceTime() != null)
                        .mapToLong(s -> s.getServiceTime().getTime()) // get milliseconds since epoch
                        .sum();

                // convert seconds to Date type
                java.sql.Time totalServiceTime = new java.sql.Time(totalMillis);

                // find the free slot which is going to be the start time of the first custom service
                LocalTime freeSlot = jobServiceHelper.freeSlotStart(previousJobs, servicePoint, totalServiceTime);

                if (freeSlot == null) {
                    jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                    return CONFLICT("No available service slots for " + prepareJob.getAppointmentDate());
                }

                LocalTime nextStartTime = freeSlot;

                //loop services and create job at point dummy list.
                for (com.flex.service_module.impl.entities.Service service: services) {
                    JobAtPoint createJobAtPoint = jobServiceHelper
                            .createJobAtPoint(servicePoint, service, job,
                                    nextStartTime, true);

                    if (createJobAtPoint != null) {
                        jobAtPointRepository.save(createJobAtPoint);
                    } else {
                        jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                        return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                    }

                    jobAtPointRepository.save(createJobAtPoint);
                    jobsAtPoint.add(createJobAtPoint);

                    nextStartTime = jobServiceHelper.calculateEndTime(nextStartTime,
                            service.getServiceTime(), servicePoint.getCloseTime());
                }

                //custom services completed
                return DATA(PreparedJob.builder()
                        .jobId(job.getId())
                        .customerId(customer.getId())
                        .appointmentDate(prepareJob.getAppointmentDate().toString())
                        .appointmentTime(freeSlot.toString())
                        .jobsAtPoint(jobsAtPoint).build());

            } else {
                log.info("{} 2 {}",Colors.YELLOW,Colors.RESET);
                LocalTime minimumStartTime = serviceCenter.getCloseTime();

                for (com.flex.service_module.impl.entities.Service service : services) {
                    long minimumServiceTimeFromSec = 86400;
                    ServicePoint suitablePoint = servicePointList.getFirst();
                    List<JobAtPoint> previousJobsAtSuitablePoint = null;

                    for (ServicePoint servicePoint : servicePointList) {
                        AvailableService availableService = availableServiceRepository
                                .availableService(service.getId(), servicePoint.getId());

                        //has service in this point?
                        if (availableService != null) {
                            List<JobAtPoint> previousJobs = jobAtPointRepository
                                    .findByJobIdAndAppointmentDate(servicePoint.getId(), prepareJob.getAppointmentDate());

                            // has previous jobs in this point
                            if (!previousJobs.isEmpty()) {
                                log.info("{} service: {}, point: {}, has other jobs {}",Colors.YELLOW,service.getName(), servicePoint.getName(),Colors.RESET);
                                List<Integer> prevJobIds = previousJobs.stream().map(
                                        j -> j.getJob().getId()
                                ).toList();

                                // has contains current job id in previous jobs
                                if (prevJobIds.contains(job.getId())) {
                                    // if has create the dummy entity and break the loop
                                    LocalTime nextStartTime = previousJobs.getLast().getEndTime();

                                    JobAtPoint createJobAtPoint = jobServiceHelper
                                            .createJobAtPoint(servicePoint, service, job,
                                                    nextStartTime, true);

                                    if (createJobAtPoint != null) {
                                        jobAtPointRepository.save(createJobAtPoint);
                                    } else {
                                        jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                                        return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                                    }

                                    if (nextStartTime.isBefore(minimumStartTime)) {
                                        minimumStartTime = nextStartTime;
                                    }

                                    jobAtPointRepository.save(createJobAtPoint);
                                    jobsAtPoint.add(createJobAtPoint);

                                } else {
                                    // if not save the total job time along with the min service time point id
                                    //      and jump to next iteration(continue).
                                    long totalSeconds = previousJobs.stream()
                                            .mapToLong(j ->
                                                    Duration.between(j.getStartTime(), j.getEndTime()).getSeconds()
                                            )
                                            .sum();

                                    if (totalSeconds < minimumServiceTimeFromSec) {
                                        log.info("{} total:{} {}",Colors.YELLOW, totalSeconds,Colors.RESET);
                                        minimumServiceTimeFromSec = totalSeconds;
                                        suitablePoint = servicePoint;
                                        previousJobsAtSuitablePoint = previousJobs;
                                    }

                                    log.info("{} previous job end time:{} {}",Colors.YELLOW, previousJobs.getLast().getEndTime(),Colors.RESET);
                                }

                            } else {
                                //  no previous jobs which means this is the better point, create the new entity and break the loop
                                LocalTime nextStartTime = servicePoint.getOpenTime();

                                JobAtPoint createJobAtPoint = jobServiceHelper
                                        .createJobAtPoint(servicePoint, service, job,
                                                nextStartTime, true);

                                if (createJobAtPoint != null) {
                                    jobAtPointRepository.save(createJobAtPoint);
                                } else {
                                    jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                                    return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                                }

                                if (nextStartTime.isBefore(minimumStartTime)) {
                                    minimumStartTime = nextStartTime;
                                }

                                jobAtPointRepository.save(createJobAtPoint);
                                jobsAtPoint.add(createJobAtPoint);
                            }
                        } //to next service point
                    }

                    // now you have the best point for this service, now you can find the best time slot for this service
                    // create the entity now. start time going to be best time slot start time.
                    LocalTime nextStartTime = jobServiceHelper
                            .freeSlotStart(previousJobsAtSuitablePoint, suitablePoint, service.getServiceTime());

                    JobAtPoint createJobAtPoint = jobServiceHelper
                            .createJobAtPoint(suitablePoint, service, job,
                                    nextStartTime, true);

                    if (createJobAtPoint != null) {
                        jobAtPointRepository.save(createJobAtPoint);
                    } else {
                        jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                        return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                    }

                    if (nextStartTime.isBefore(minimumStartTime)) {
                        minimumStartTime = nextStartTime;
                    }

                    jobAtPointRepository.save(createJobAtPoint);
                    jobsAtPoint.add(createJobAtPoint);
                }

                //custom services completed
                return DATA(PreparedJob.builder()
                        .jobId(job.getId())
                        .customerId(customer.getId())
                        .appointmentDate(prepareJob.getAppointmentDate().toString())
                        .appointmentTime(minimumStartTime != null ? minimumStartTime.toString() : null)
                        .jobsAtPoint(jobsAtPoint).build());
            }
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
            jobAtPointRepository.flush();
        }

        jobRepository.delete(job);
        jobRepository.flush();

        customerRepository.delete(customer);
        customerRepository.flush();

        return SUCCESS("");
    }

    @Override
    public ResponseEntity<?> pointWiseJobs(PointJobs pointJobs, HttpServletRequest request) {
        log.info(request.getRequestURI());

        ServiceCenter serviceCenter = serviceCenterRepository.findByIdAndDeletedIsFalse(pointJobs.getServiceCenter());

        if (serviceCenter == null) {
            return CONFLICT("Service center not found");
        }

        List<ServicePoint> servicePoints = servicePointRepository.servicePointsByCenter(pointJobs.getServiceCenter());

        List<JobsSchedule> jobsSchedules = new ArrayList<>();

        if (!servicePoints.isEmpty()) {

            int id = 1;

            for (ServicePoint servicePoint : servicePoints) {

                long servicePointOpeningDurationBySec = Duration
                        .between(servicePoint.getOpenTime(), servicePoint.getCloseTime())
                        .getSeconds();

                List<JobTimelineProjection> jobTimelineProjections = jobAtPointRepository
                        .getJobTimeline(servicePoint.getId(), pointJobs.getDate());

                if (jobTimelineProjections.isEmpty()) {
                    // no jobs in this service point yet
                    JobsSchedule freeSlot = JobsSchedule.builder()
                            .id(id)
                            .pointName(servicePoint.getName())
                            .totalTime(100)
                            .fromTo(servicePoint.getOpenTime() + " - " + servicePoint.getCloseTime())
                            .freeSlot(true)
                            .build();
                    id++;

                    jobsSchedules.add(freeSlot);
                } else {

                    LocalTime lastEndTime = servicePoint.getOpenTime();
                    LocalTime sameJobStartTime = null;
                    int lastFreeTimeIndicator = 0;
                    Integer prevJobId = null;

                    for (JobTimelineProjection jobTimelineProjection : jobTimelineProjections) {

                        // check the last end time and this job start time has gap
                        if (lastEndTime.isBefore(jobTimelineProjection.getStartTime())) {
                            // if has, create a free slot before this job create.
                            long freeTimeDurationFromSec = Duration
                                    .between(lastEndTime, jobTimelineProjection.getStartTime())
                                    .getSeconds();

                            double freeTimePercent =
                                    (freeTimeDurationFromSec * 100.0)
                                            / servicePointOpeningDurationBySec;

                            int freeTimeDurationFromPercent = (int) Math.round(freeTimePercent);

                            JobsSchedule freeSlot = JobsSchedule.builder()
                                    .id(id)
                                    .pointName(servicePoint.getName())
                                    .totalTime(freeTimeDurationFromPercent)
                                    .fromTo(lastEndTime + " - " + jobTimelineProjection.getStartTime())
                                    .freeSlot(true)
                                    .build();
                            id++;

                            jobsSchedules.add(freeSlot);

                        }

                        if (prevJobId != null && prevJobId.equals(jobTimelineProjection.getJobId())) {

                            log.info("prevId: {}", prevJobId);
                            log.info("currId: {}", jobTimelineProjection.getJobId());
                            log.info("length: {}", jobsSchedules.size() - 1);
                            log.info("id: {}", id);

                            JobsSchedule existingSchedule =
                                    jobsSchedules.getLast();

                            // then create this job slot
                            long jobTimeDurationFromSec = Duration
                                    .between(jobTimelineProjection.getStartTime(), jobTimelineProjection.getEndTime())
                                    .getSeconds();

                            double jobPercent =
                                    (jobTimeDurationFromSec * 100.0)
                                            / servicePointOpeningDurationBySec;

                            int jobTimeDurationFromPercent = (int) Math.round(jobPercent);

                            existingSchedule.setTotalTime(existingSchedule.getTotalTime() + jobTimeDurationFromPercent);
                            existingSchedule.setFromTo(sameJobStartTime + " - " + jobTimelineProjection.getEndTime());

                        } else {

                            if (prevJobId == null) {
                                prevJobId = jobTimelineProjection.getJobId();
                            }

                            sameJobStartTime = jobTimelineProjection.getStartTime();

                            // then create this job slot
                            long jobTimeDurationFromSec = Duration
                                    .between(jobTimelineProjection.getStartTime(), jobTimelineProjection.getEndTime())
                                    .getSeconds();

                            double jobPercent =
                                    (jobTimeDurationFromSec * 100.0)
                                            / servicePointOpeningDurationBySec;

                            int jobTimeDurationFromPercent = (int) Math.round(jobPercent);

                            // then after create job
                            JobsSchedule jobSlot = JobsSchedule.builder()
                                    .id(id)
                                    .jobAtPointId(jobTimelineProjection.getJobAtPointId())
                                    .jobId(jobTimelineProjection.getJobId())
                                    .customerName(jobTimelineProjection.getCustomerName())
                                    .pointName(servicePoint.getName())
                                    .serviceName(jobTimelineProjection.getServiceName())
                                    .status(jobTimelineProjection.getStatus())
                                    .totalTime(jobTimeDurationFromPercent)
                                    .fromTo(jobTimelineProjection.getStartTime() + " - " + jobTimelineProjection.getEndTime())
                                    .freeSlot(false)
                                    .build();

                            jobsSchedules.add(jobSlot);

                            id++;
                        }

                        lastEndTime = jobTimelineProjection.getEndTime();

                        if (lastFreeTimeIndicator == jobTimelineProjections.size() - 1) {
                            long freeTimeDurationFromSec = Duration
                                    .between(lastEndTime, servicePoint.getCloseTime())
                                    .getSeconds();

                            if (freeTimeDurationFromSec > 0) {
                                double freeTimePercent =
                                        (freeTimeDurationFromSec * 100.0)
                                                / servicePointOpeningDurationBySec;

                                int freeTimeDurationFromPercent = (int) Math.round(freeTimePercent);

                                JobsSchedule freeSlot = JobsSchedule.builder()
                                        .id(id)
                                        .pointName(servicePoint.getName())
                                        .totalTime(freeTimeDurationFromPercent)
                                        .fromTo(lastEndTime + " - " + servicePoint.getCloseTime())
                                        .freeSlot(true)
                                        .build();

                                jobsSchedules.add(freeSlot);
                            }
                        }
                        lastFreeTimeIndicator++;
                    }
                }
            }

            return DATA(jobsSchedules);
        }

        return DATA(null);
    }
}
