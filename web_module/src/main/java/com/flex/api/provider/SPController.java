package com.flex.api.provider;

import com.flex.service_module.api.services.SPService;
import com.flex.service_module.impl.entities.ServiceProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    private final SPService SPService;

    @GetMapping("/profile")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> profile(HttpServletRequest request) {
        return SPService.serviceProviderProfile(request);
    }

    @PostMapping("/edit")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).SP)")
    public ResponseEntity<?> editSP(@RequestBody ServiceProvider serviceProvider , HttpServletRequest request) {
        return SPService.editServiceProvider(serviceProvider, request);
    }
}
