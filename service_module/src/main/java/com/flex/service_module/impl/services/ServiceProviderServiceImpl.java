package com.flex.service_module.impl.services;

import com.flex.common_module.http.ReturnResponse;
import com.flex.common_module.security.http.response.UserClaims;
import com.flex.common_module.security.utils.JwtUtil;
import com.flex.service_module.api.services.ServiceProviderService;
import com.flex.service_module.impl.entities.ServiceProvider;
import com.flex.service_module.impl.repositories.ServiceProviderRepository;
import com.flex.service_module.impl.services.helpers.SPServiceHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.flex.common_module.http.ReturnResponse.*;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class ServiceProviderServiceImpl implements ServiceProviderService {

    private final SPServiceHelper spServiceHelper;

    private final ServiceProviderRepository serviceProviderRepository;

    @Override
    public ResponseEntity<?> editServiceProvider(ServiceProvider serviceProvider, HttpServletRequest request) {
        log.info(request.getRequestURI() + " body: " + serviceProvider);

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider existing = serviceProviderRepository.findByProviderIdAndDeletedIsFalse(serviceProvider.getProviderId());

        if (existing == null) {
            return CONFLICT("Service provider not found");
        }

        if (serviceProviderRepository.existsByNameAndEmailAndDeletedIsFalse(serviceProvider.getName(),
                serviceProvider.getEmail())) {
            return CONFLICT("Name and Email already exist");
        }

        if (spServiceHelper.isValidAndDifferent(serviceProvider.getName(), existing.getName())) {
            existing.setName(serviceProvider.getName());
        }

        if (spServiceHelper.isValidAndDifferent(serviceProvider.getEmail(), existing.getEmail())) {
            existing.setEmail(serviceProvider.getEmail());
        }

        if (spServiceHelper.isValidAndDifferent(serviceProvider.getContact(), existing.getContact())) {
            existing.setContact(serviceProvider.getContact());
        }

        if (spServiceHelper.isValidAndDifferent(serviceProvider.getAddress(), existing.getAddress())) {
            existing.setAddress(serviceProvider.getAddress());
        }

        if (spServiceHelper.isValidAndDifferent(serviceProvider.getWebsite(), existing.getWebsite())) {
            existing.setWebsite(serviceProvider.getWebsite());
        }

        if (spServiceHelper.isValidAndDifferent(serviceProvider.getDescription(), existing.getDescription())) {
            existing.setDescription(serviceProvider.getDescription());
        }

        serviceProviderRepository.save(existing);

        return SUCCESS("Service provider updated");
    }
}
