package com.flex.job_module.impl.services;

import com.flex.common_module.constants.Colors;
import com.flex.common_module.security.http.response.UserClaims;
import com.flex.common_module.security.utils.JwtUtil;
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
import com.flex.service_module.impl.entities.ServiceProvider;
import com.flex.service_module.impl.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ServiceProviderRepository serviceProviderRepository;

    @Transactional
    @Override
    public ResponseEntity<?> prepareJob(PrepareJob prepareJob, HttpServletRequest request) {
        log.info(request.getRequestURI());

        ServiceCenter serviceCenter = serviceCenterRepository.findByIdAndDeletedIsFalse(prepareJob.getServiceCenterId());

        if (serviceCenter == null) {
            return CONFLICT("Service center not found");
        }

        List<ServicePoint> servicePointList = servicePointRepository.servicePointsByCenter(serviceCenter.getId());

        if (servicePointList.isEmpty()) {
            return CONFLICT("Service points not found");
        }

        // this take as all service points are empty at the start, this is using to address the nextStartTime issue
        // for empty service points.
        // * if the service point has no jobs, the nextStartTime must be equal to the last created jobs end time
        List<Integer> emptyServicePoints =
                new ArrayList<>(
                        servicePointList.stream()
                                .map(ServicePoint::getId)
                                .toList()
                );

        Customer customer = customerRepository.findByPhone(prepareJob.getPhone());

        if (customer == null) {
            customer = Customer.builder()
                    .customer(prepareJob.getCustomer())
                    .phone(prepareJob.getPhone())
                    .dummy(true)
                    .build();
        }

        Job hasJobForThisCustomer = jobRepository
                .jobForCustomer(customer.getId());

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

            log.info(" -- cluster through -- ");

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

            // loop services and check create the job at point
            for (com.flex.service_module.impl.entities.Service centerClusterService : centerClusterServices) {
                long minimumServiceTimeFromSec = 86400;
                ServicePoint suitablePoint = servicePointList.getFirst();
                LocalTime minimumEndTime = null;

                // this is using for avoid create other jobs after breaking the loop. check the usage
                int i = 0;
                log.info(" ");
                log.info("{}service: {}{}", Colors.YELLOW, centerClusterService.getName(), Colors.RESET);
                for (ServicePoint servicePoint : servicePointList) {
                    log.info("{}point: {}{}", Colors.YELLOW, servicePoint.getName(), Colors.RESET);
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
                                    log.info("point: {}", servicePoint.getName() + " ✅");
                                    log.info("job start time: {}" ,nextStartTime);
                                    log.info("job end time: {}", createJobAtPoint.getEndTime());
                                    log.info("Chosen by: has prev jobs in this point");
                                    jobAtPointRepository.save(createJobAtPoint);
                                } else {
                                    jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                                    return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                                }

                                jobsAtPoint.add(createJobAtPoint);
                                nextStartTime = createJobAtPoint.getEndTime();
                                log.info("next start time going to be: {}", nextStartTime);
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

                                            List<Integer> jobAtPointIds = jobAtPointRepository
                                                    .getPendingJobAtPointIdsByPoint(id, prepareJob.getAppointmentDate());

                                            if (jobAtPointIds == null || jobAtPointIds.isEmpty()) {
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
                                log.info("point: {}", servicePoint.getName() + " ✅");
                                log.info("job start time: {}" ,nextStartTime);
                                log.info("job end time: {}", createJobAtPoint.getEndTime());
                                log.info("Chosen by: no prev jobs in this point");
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
                            log.info("next start time going to be: {}", nextStartTime);
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

                    LocalTime bestTime;

                    if (minimumEndTime != null && minimumEndTime.isBefore(nextStartTime)) {
                        bestTime = minimumEndTime;
                    } else {
                        bestTime = nextStartTime;
                    }

                    JobAtPoint createJobAtPoint = jobServiceHelper
                            .createJobAtPoint(suitablePoint, centerClusterService, job,
                                    bestTime, true);

                    if (createJobAtPoint != null) {
                        log.info("point: {}", suitablePoint.getName() + " ✅");
                        log.info("job start time: {}" ,bestTime);
                        log.info("job end time: {}", createJobAtPoint.getEndTime());
                        log.info("Chosen by: minimum service time check");
                        jobAtPointRepository.save(createJobAtPoint);
                    } else {
                        jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                        return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                    }

                    nextStartTime = createJobAtPoint.getEndTime();
                    log.info("next start time going to be: {}", nextStartTime);
                    allStartTimes.add(nextStartTime);
                    jobAtPointRepository.save(createJobAtPoint);

                    jobsAtPoint.add(createJobAtPoint);
                }
                log.info(" ");
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

            log.info(" ");
            log.info(" -- custom services -- ");

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

                log.info(" -- all services has some point/points -- ");

                // calculate the total service time for custom services
                long totalSeconds = services.stream()
                        .filter(s -> s.getServiceTime() != null)
                        .mapToLong(s -> s.getServiceTime().toSecondOfDay()) // seconds of the day
                        .sum();

                // convert seconds to Date type
                LocalTime totalServiceTime = LocalTime.ofSecondOfDay(totalSeconds);

                log.info(" ");
                log.info("total service time: {} ", totalServiceTime);

                LocalTime freeSlot = null;
                ServicePoint bestPoint = null;

                for (ServicePoint servicePoint: points) {
                    // get the previous jobs of the point which has minimum service time
                    List<JobAtPoint> previousJobs = jobAtPointRepository
                            .findByJobIdAndAppointmentDate(servicePoint.getId(), prepareJob.getAppointmentDate());

                    // find the free slot which is going to be the start time of the first custom service
                    freeSlot = jobServiceHelper.findFreeSlot(previousJobs, servicePoint, totalServiceTime);

                    if (freeSlot != null) {
                        log.info("found free slot among jobs ✅");
                        bestPoint = servicePoint;
                        break;
                    }
                }

                if (freeSlot == null) {
                    List<Integer> pointIds = servicePointList.stream().map(
                            ServicePoint::getId
                    ).collect(Collectors.toList());

                    // find the service point which has the lowest service time
                    MinimumServiceTimePoint lowestServiceTimePoint = jobAtPointRepository
                            .findServicePointWithMinTotalServiceTime(pointIds, prepareJob.getServicesIds());

                    bestPoint = servicePointRepository.findByIdAndDeletedIsFalse(
                            lowestServiceTimePoint.getServicePointId()
                    );

                    List<JobAtPoint> previousJobsAtSuitablePoint = jobAtPointRepository
                            .getPendingJobsAtPointByPoint(lowestServiceTimePoint.getServicePointId(),
                                    prepareJob.getAppointmentDate());

                    if (previousJobsAtSuitablePoint == null || previousJobsAtSuitablePoint.isEmpty()) {
                        log.info("found free slot in empty point ✅");
                        freeSlot = bestPoint.getOpenTime();
                    } else {
                        log.info("assign job to point which has the lowest service time ✅");
                        for (JobAtPoint jobAtPoint: previousJobsAtSuitablePoint) {
                            log.info("job: {}", jobAtPoint.getService().getName());
                            log.info("start time: {}", jobAtPoint.getStartTime());
                            log.info("end time: {}", jobAtPoint.getEndTime());
                            log.info("last job id: {}", jobAtPoint.getJob().getId());
                            log.info("job id: {}", job.getId());
                        }
                        freeSlot = previousJobsAtSuitablePoint.getLast().getEndTime();
                    }
                }

                log.info("free slot start time: {} ", freeSlot);
                log.info("chosen point: {} ", bestPoint.getName() + " ✅");

                if (freeSlot == null) {
                    jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                    return CONFLICT("No available service slots for " + prepareJob.getAppointmentDate());
                }

                LocalTime nextStartTime = freeSlot;

                //loop services and create job at point dummy list.
                for (com.flex.service_module.impl.entities.Service service: services) {

                    log.info(" ");
                    log.info("service: {}", service.getName());
                    JobAtPoint createJobAtPoint = jobServiceHelper
                            .createJobAtPoint(bestPoint, service, job,
                                    nextStartTime, true);

                    if (createJobAtPoint != null) {
                        log.info("start time: {}", nextStartTime);
                        log.info("end time: {}", createJobAtPoint.getEndTime());
                        jobAtPointRepository.save(createJobAtPoint);
                    } else {
                        jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                        log.info(Colors.YELLOW + "4" + Colors.RESET);
                        return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                    }

                    jobAtPointRepository.save(createJobAtPoint);
                    jobsAtPoint.add(createJobAtPoint);

                    nextStartTime = jobServiceHelper.calculateEndTime(nextStartTime,
                            service.getServiceTime(), bestPoint.getCloseTime());
                    log.info("next start time going to be: {}", nextStartTime);
                    log.info(" ");
                }

                //custom services completed
                return DATA(PreparedJob.builder()
                        .jobId(job.getId())
                        .customerId(customer.getId())
                        .appointmentDate(prepareJob.getAppointmentDate().toString())
                        .appointmentTime(freeSlot.toString())
                        .jobsAtPoint(jobsAtPoint).build());

            } else {
                log.info(" ");
                log.info(" -- services are in different points -- ");
                LocalTime minimumStartTime = serviceCenter.getCloseTime();

                LocalTime nextStartTime = serviceCenter.getOpenTime();

                for (com.flex.service_module.impl.entities.Service service : services) {
                    log.info(" ");
                    log.info("{}service: {}{}", Colors.YELLOW, service.getName(), Colors.RESET);
                    int i = 0;
                    long minimumServiceTimeFromSec = 86400;
                    ServicePoint suitablePoint = servicePointList.getFirst();

                    for (ServicePoint servicePoint : servicePointList) {

                        log.info("{}service point: {}{}", Colors.YELLOW, servicePoint.getName(), Colors.RESET);
                        AvailableService availableService = availableServiceRepository
                                .availableService(service.getId(), servicePoint.getId());

                        //has service in this point?
                        if (availableService != null) {
                            List<JobAtPoint> previousJobs = jobAtPointRepository
                                    .findByJobIdAndAppointmentDate(servicePoint.getId(), prepareJob.getAppointmentDate());

                            // has previous jobs in this point
                            if (!previousJobs.isEmpty()) {
                                List<Integer> prevJobIds = previousJobs.stream().map(
                                        j -> j.getJob().getId()
                                ).toList();

                                // has contains current job id in previous jobs
                                if (prevJobIds.contains(job.getId())) {
                                    // if has create the dummy entity and break the loop
                                    nextStartTime = previousJobs.getLast().getEndTime();

                                    JobAtPoint createJobAtPoint = jobServiceHelper
                                            .createJobAtPoint(servicePoint, service, job,
                                                    nextStartTime, true);

                                    if (createJobAtPoint != null) {
                                        log.info("point: {}", servicePoint.getName() + " ✅");
                                        log.info("service time: {}", service.getServiceTime());
                                        log.info("job start time: {}" ,nextStartTime);
                                        log.info("job end time: {}", createJobAtPoint.getEndTime());
                                        log.info("Chosen by: has prev jobs in this point");
                                        jobAtPointRepository.save(createJobAtPoint);
                                    } else {
                                        jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                                        log.info(Colors.YELLOW + "5" + Colors.RESET);
                                        return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                                    }

                                    if (nextStartTime.isBefore(previousJobs.getLast().getEndTime())) {
                                        log.info("nextStartTime: {}", nextStartTime);

                                        nextStartTime = previousJobs.getLast().getEndTime();
                                        log.info("end: {}", nextStartTime);
                                    }

                                    jobAtPointRepository.save(createJobAtPoint);
                                    jobsAtPoint.add(createJobAtPoint);
                                    log.info("next start time going to be: {}", nextStartTime);
                                    break;
                                } else {
                                    // if not save the total job time along with the min service time point id
                                    //      and jump to next iteration(continue).
                                    long totalSeconds = previousJobs.stream()
                                            .mapToLong(j ->
                                                    Duration.between(j.getStartTime(), j.getEndTime()).getSeconds()
                                            )
                                            .sum();

                                    if (totalSeconds < minimumServiceTimeFromSec) {
                                        minimumServiceTimeFromSec = totalSeconds;
                                        suitablePoint = servicePoint;
                                        nextStartTime = previousJobs.getLast().getEndTime();
                                    }
                                }

                            } else {
                                //  no previous jobs which means this is the better point, create the new entity and break the loop

                                JobAtPoint createJobAtPoint = jobServiceHelper
                                        .createJobAtPoint(servicePoint, service, job,
                                                nextStartTime, true);

                                if (createJobAtPoint != null) {
                                    log.info("point: {}", servicePoint.getName() + " ✅");
                                    log.info("service time: {}", service.getServiceTime());
                                    log.info("job start time: {}" ,nextStartTime);
                                    log.info("job end time: {}", createJobAtPoint.getEndTime());
                                    log.info("Chosen by: no prev jobs in this point");
                                    jobAtPointRepository.save(createJobAtPoint);
                                } else {
                                    jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                                    return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                                }

                                nextStartTime = jobServiceHelper.calculateEndTime(nextStartTime,
                                        service.getServiceTime(), servicePoint.getCloseTime());

                                jobsAtPoint.add(createJobAtPoint);
                                log.info("next start time going to be: {}", nextStartTime);
                                break;
                            }
                        } //to next service point
                        i = i + 1;
                    }

                    if (servicePointList.size() == i) {

                        JobAtPoint createJobAtPoint = jobServiceHelper
                                .createJobAtPoint(suitablePoint, service, job,
                                        nextStartTime, true);

                        if (createJobAtPoint != null) {
                            log.info("point: {}", suitablePoint.getName() + " ✅");
                            log.info("service time: {}", service.getServiceTime());
                            log.info("job start time: {}" ,nextStartTime);
                            log.info("job end time: {}", createJobAtPoint.getEndTime());
                            log.info("Chosen by: minimum service time check");
                            jobAtPointRepository.save(createJobAtPoint);
                        } else {
                            jobServiceHelper.clearDummyData(customer.getId(), job.getId());
                            return CONFLICT("Sorry, No available service slots for " + prepareJob.getAppointmentDate());
                        }

                        nextStartTime = createJobAtPoint.getEndTime();

                        if (nextStartTime.isBefore(minimumStartTime)) {
                            minimumStartTime = nextStartTime;
                        }

                        jobAtPointRepository.save(createJobAtPoint);
                        jobsAtPoint.add(createJobAtPoint);
                        log.info("next start time going to be: {}", nextStartTime);
                    }
                    log.info(" ");
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

            List<Integer> spIds = servicePoints.stream().map(ServicePoint::getId).toList();

            LocalTime minimumServiceTime = availableServiceRepository.findMinimumServiceTimeByServicePointIds(spIds);

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

                            boolean ignoreThis = freeTimeDurationFromSec <= minimumServiceTime.toSecondOfDay();

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
                                    .ignoreThis(ignoreThis)
                                    .build();
                            id++;

                            jobsSchedules.add(freeSlot);

                        }

                        if (prevJobId != null && prevJobId.equals(jobTimelineProjection.getJobId())) {

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

                                boolean ignoreThis = freeTimeDurationFromSec <= minimumServiceTime.toSecondOfDay();

                                int freeTimeDurationFromPercent = (int) Math.round(freeTimePercent);

                                JobsSchedule freeSlot = JobsSchedule.builder()
                                        .id(id)
                                        .pointName(servicePoint.getName())
                                        .totalTime(freeTimeDurationFromPercent)
                                        .fromTo(lastEndTime + " - " + servicePoint.getCloseTime())
                                        .freeSlot(true)
                                        .ignoreThis(ignoreThis)
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

    @Override
    @Scheduled(fixedRate = 60000)
    public void deleteExpiredDummyJobs() {
        LocalDate date = LocalDate.now();

        List<Integer> dummyDataIds = jobAtPointRepository
                .getDummyJobIds(date);

        log.info("dummy jobs checking \uD83D\uDD0E");
        if (!dummyDataIds.isEmpty()) {
            log.info("has dummy jobs");
            LocalTime time = LocalTime.now().minusMinutes(10);

            List<JobAtPoint> expiredJobsAtPoints = jobAtPointRepository.findExpiredDummy(date, time);

            List<Job> expiredJobs = expiredJobsAtPoints.stream().map(
                    JobAtPoint::getJob
            ).toList();

            List<Customer> expiredCustomers = expiredJobs.stream().map(
                    Job::getCustomer
            ).toList();

            if (!expiredJobsAtPoints.isEmpty()) {
                log.info("{} jobs at points destroying \uD83D\uDD25", expiredJobsAtPoints.size());
                jobAtPointRepository.deleteAll(expiredJobsAtPoints);
                jobAtPointRepository.flush();
                log.info("jobs at points destroyed ✅");
            }

            if (!expiredJobs.isEmpty()) {
                log.info("{} jobs destroying \uD83D\uDD25", expiredJobs.size());
                jobRepository.deleteAll(expiredJobs);
                jobAtPointRepository.flush();
                log.info("jobs destroyed ✅");
            }

            if (!expiredCustomers.isEmpty()) {
                log.info("{} customers destroying \uD83D\uDD25", expiredCustomers.size());
                customerRepository.deleteAll(expiredCustomers);
                jobAtPointRepository.flush();
                log.info("customers destroyed ✅");
            }
        } else {
            log.info("no dummy jobs");
        }
    }
}
