package com.flex.service_module.impl.services;

import com.flex.common_module.http.pagination.Pagination;
import com.flex.common_module.http.pagination.Sorting;
import com.flex.common_module.security.http.response.UserClaims;
import com.flex.common_module.security.utils.JwtUtil;
import com.flex.service_module.api.http.DTO.classes.ServiceViewDTO;
import com.flex.service_module.api.http.requests.AssignServiceToPoint;
import com.flex.service_module.api.services.ServicesService;
import com.flex.service_module.impl.entities.AvailableService;
import com.flex.service_module.impl.entities.ServicePoint;
import com.flex.service_module.impl.entities.ServiceProvider;
import com.flex.service_module.impl.repositories.*;
import com.flex.service_module.impl.services.helpers.ServicesServiceHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.flex.common_module.http.ReturnResponse.*;;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/2/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class ServicesServiceImpl implements ServicesService {

    private final ServicesServiceHelper servicesServiceHelper;

    private final ServicesRepository servicesRepository;
    private final ServicePointRepository servicePointRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final AvailableServiceRepository availableServiceRepository;
    private final ServiceCenterRepository serviceCenterRepository;

    @Override
    public ResponseEntity<?> addService(com.flex.service_module.impl.entities.Service service, HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Service provider not found");
        }

        // 🔒 Check duplicate service name
        if (servicesRepository.existsByNameAndProvider_IdAndDeletedIsFalse(
                service.getName(), provider.getId()
        )) {
            return CONFLICT("Service already exists");
        }

        // 🧹 Optional validations
        if (service.getName() == null || service.getName().trim().isEmpty()) {
            return CONFLICT("Service name is required");
        }

        if (service.getDownPrice() == null || service.getDownPrice() <= 0) {
            return CONFLICT("Invalid down price");
        }

        // 🧠 Set system-managed fields
        service.setProvider(provider);
        service.setDeleted(false);

        servicesRepository.save(service);

        return SUCCESS("Service created successfully");
    }

    @Override
    public ResponseEntity<?> updateService(com.flex.service_module.impl.entities.Service service, HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Service provider not found");
        }

        com.flex.service_module.impl.entities.Service existing = servicesRepository
                .findById(service.getId()).orElse(null);

        if (existing == null || existing.isDeleted()) {
            return CONFLICT("Service not found");
        }

        // 🔐 Ownership check
        if (!existing.getProvider().getId().equals(provider.getId())) {
            return CONFLICT("Unauthorized access");
        }

        boolean updated = false;

        // ✅ Update name
        if (service.getName() != null
                && !service.getName().trim().isEmpty()
                && !service.getName().equals(existing.getName())) {

            if (servicesRepository.existsByNameAndProvider_IdAndDeletedIsFalseAndIdNot(
                    service.getName(), provider.getId(), existing.getId()
            )) {
                return CONFLICT("Service name already exists");
            }

            existing.setName(service.getName());
            updated = true;
        }

        // ✅ Update description
        if (service.getDescription() != null
                && !service.getDescription().trim().isEmpty()
                && !service.getDescription().equals(existing.getDescription())) {

            existing.setDescription(service.getDescription());
            updated = true;
        }

        // ✅ Update service time
        if (service.getServiceTime() != null
                && !service.getServiceTime().equals(existing.getServiceTime())) {

            existing.setServiceTime(service.getServiceTime());
            updated = true;
        }

        // ✅ Update total price
        if (service.getTotalPrice() != null
                && !service.getTotalPrice().equals(existing.getTotalPrice())) {

            existing.setTotalPrice(service.getTotalPrice());
            updated = true;
        }

        // ✅ Update down price
        if (service.getDownPrice() != null
                && !service.getDownPrice().equals(existing.getDownPrice())) {

            existing.setDownPrice(service.getDownPrice());
            updated = true;
        }

        if (existing.isServiceTimeDepends() != service.isServiceTimeDepends()) {
            existing.setServiceTimeDepends(service.isServiceTimeDepends());
            updated = true;
        }

        if (existing.isTotalPriceDepends() != service.isTotalPriceDepends()) {
            existing.setTotalPriceDepends(service.isTotalPriceDepends());
            updated = true;
        }

        if (!updated) {
            return SUCCESS("No changes detected");
        }

        servicesRepository.save(existing);

        return SUCCESS("Service updated successfully");
    }

    @Override
    public ResponseEntity<?> availableServicesForPoint(Integer servicePointId, HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Service provider not found");
        }

        ServicePoint servicePoint = servicePointRepository.findByIdAndDeletedIsFalse(servicePointId);

        if (servicePoint == null) {
            return CONFLICT("Service point not found");
        }

        List<AvailableService> assignedServices = availableServiceRepository.findAllByServicePointId(servicePointId);

        List<Integer> unavailableServicesIds = assignedServices.stream().map(s -> s.getService().getId())
                .toList();

        List<com.flex.service_module.impl.entities.Service> allServices = servicesRepository
                .findAllByProvider_IdAndDeletedIsFalse(provider.getId());

        if (allServices == null || allServices.isEmpty()) {
            return CONFLICT("No available services");
        }

        List<com.flex.service_module.impl.entities.Service> availableServices = allServices.stream().filter(
                s -> !unavailableServicesIds.contains(s.getId())
        ).toList();

        return DATA(availableServices);
    }

    @Override
    public ResponseEntity<?> assignedServicesForPoint(Integer servicePointId, HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Service provider not found");
        }

        ServicePoint servicePoint = servicePointRepository.findByIdAndDeletedIsFalse(servicePointId);

        if (servicePoint == null) {
            return CONFLICT("Service point not found");
        }

        List<AvailableService> assignedServices = availableServiceRepository.findAllByServicePointId(servicePointId);

        return DATA(
                assignedServices.stream().map(
                        AvailableService::getService
                ).toList()
        );
    }

    @Override
    public ResponseEntity<?> assignedPointsForService(Integer service, HttpServletRequest request) {
        log.info(request.getRequestURI());

        if (!servicesRepository.existsByIdAndDeletedIsFalse(service)) {
            return CONFLICT("Service not found");
        }

        return DATA(availableServiceRepository.findPointsByServiceId(service));
    }

    @Override
    public ResponseEntity<?> assignServicesForPoint(AssignServiceToPoint assignServiceToPoint, HttpServletRequest request) {
        log.info(request.getRequestURI());

        ServicePoint servicePoint = servicePointRepository
                .findByIdAndDeletedIsFalse(assignServiceToPoint.getPointId());

        if (servicePoint == null) {
            return CONFLICT("Service point not found");
        }

        com.flex.service_module.impl.entities.Service service = servicesRepository
                .findByIdAndDeletedIsFalse(assignServiceToPoint.getServiceId());

        if (service == null) {
            return CONFLICT("Service not found");
        }

        AvailableService availableService = AvailableService.builder()
                .service(service)
                .servicePoint(servicePoint)
                .build();

        availableServiceRepository.save(availableService);

        return SUCCESS("Service assigned successfully");
    }

    @Override
    public ResponseEntity<?> removeServicesFromPoint(AssignServiceToPoint assignServiceToPoint, HttpServletRequest request) {
        log.info(request.getRequestURI());

        ServicePoint servicePoint = servicePointRepository
                .findByIdAndDeletedIsFalse(assignServiceToPoint.getPointId());

        if (servicePoint == null) {
            return CONFLICT("Service point not found");
        }

        com.flex.service_module.impl.entities.Service service = servicesRepository
                .findByIdAndDeletedIsFalse(assignServiceToPoint.getServiceId());

        if (service == null) {
            return CONFLICT("Service not found");
        }

        AvailableService availableService = availableServiceRepository.availableService(service.getId(), servicePoint.getId());

        if (availableService == null) {
            return CONFLICT("This service is not assigned to this point");
        }

        availableServiceRepository.delete(availableService);

        return SUCCESS("Service removed successfully");
    }

    @Override
    public ResponseEntity<?> getAllServices(Pagination pagination, HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Service provider not found");
        }

        Sort sort = Sort.by(Sorting.getSort(pagination.getSort()));

        Pageable pageable = PageRequest.of(
                pagination.getPage(),
                pagination.getSize(),
                sort
        );

        Page<com.flex.service_module.impl.entities.Service> result = servicesRepository.findAllWithSearch(
                provider.getId(),
                pagination.getSearchText() == null
                        ? null
                        : pagination.getSearchText().trim(),
                pageable
        );

        return DATA(
                result.map(s -> ServiceViewDTO.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .serviceTime(s.getServiceTime())
                        .serviceTimeDepends(s.isServiceTimeDepends())
                        .fServiceTime(servicesServiceHelper.formatDuration(s.getServiceTime()))
                        .totalPrice(s.getTotalPrice())
                        .totalPriceDepends(s.isTotalPriceDepends())
                        .downPrice(s.getDownPrice())
                        .build())
        );
    }

    @Override
    public ResponseEntity<?> deleteService(Integer id, HttpServletRequest request) {
        log.info(request.getRequestURI());

        com.flex.service_module.impl.entities.Service service = servicesRepository.findByIdAndDeletedIsFalse(id);

        if (service == null) {
            return CONFLICT("Service not found");
        }

        service.setDeleted(true);

        servicesRepository.save(service);

        return SUCCESS("Service deleted successfully");
    }
}
