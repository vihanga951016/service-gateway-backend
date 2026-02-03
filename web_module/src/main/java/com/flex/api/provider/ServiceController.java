package com.flex.api.provider;

import com.flex.common_module.http.pagination.Pagination;
import com.flex.service_module.api.http.requests.AssignServiceToPoint;
import com.flex.service_module.api.services.ServicesService;
import com.flex.service_module.impl.entities.Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/2/2026
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServicesService servicesService;

    @PostMapping("/add")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).SM)")
    public ResponseEntity<?> add(@RequestBody Service service, HttpServletRequest request) {
        return servicesService.addService(service, request);
    }

    @PostMapping("/update")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).SM)")
    public ResponseEntity<?> update(@RequestBody Service service, HttpServletRequest request) {
        return servicesService.updateService(service, request);
    }

    @PostMapping("/get-all")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> getAll(@RequestBody Pagination pagination, HttpServletRequest request) {
        return servicesService.getAllServices(pagination, request);
    }

    @GetMapping("/not-assign-to-point/{pointId}")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> notAssignToPoint(@PathVariable Integer pointId, HttpServletRequest request) {
        return servicesService.nonAssignedServicesForPoint(pointId, request);
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).SM)")
    public ResponseEntity<?> delete(@PathVariable Integer id, HttpServletRequest request) {
        return servicesService.deleteService(id, request);
    }

    @PostMapping("/assign-to-point")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PM)")
    public ResponseEntity<?> delete(@RequestBody AssignServiceToPoint assignServiceToPoint, HttpServletRequest request) {
        return servicesService.assignServicesToPoint(assignServiceToPoint, request);
    }
}
