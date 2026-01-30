package com.flex.api.provider;

import com.flex.service_module.api.services.ServiceProviderService;
import com.flex.service_module.impl.entities.ServiceProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/30/2026
 */
@RestController
@RequestMapping("/service-provider")
@RequiredArgsConstructor
public class SPController {

    private final ServiceProviderService serviceProviderService;

    @GetMapping("/edit")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).SP)")
    public ResponseEntity<?> editSP(ServiceProvider serviceProvider , HttpServletRequest request) {
        return serviceProviderService.editServiceProvider(serviceProvider, request);
    }
}
