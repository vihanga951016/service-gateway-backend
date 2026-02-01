package com.flex.api.provider;

import com.flex.common_module.http.pagination.Pagination;
import com.flex.service_module.api.services.SCService;
import com.flex.service_module.impl.entities.ServiceCenter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/1/2026
 */
@RestController
@RequestMapping("/service-center")
@RequiredArgsConstructor
public class SCController {

    private final SCService scService;

    @PostMapping("/add")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> add(@RequestBody ServiceCenter serviceCenter, HttpServletRequest request) {
        return scService.addNewCenter(serviceCenter, request);
    }

    @PostMapping("/update")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> update(@RequestBody ServiceCenter serviceCenter, HttpServletRequest request) {
        return scService.updateCenter(serviceCenter, request);
    }

    @PostMapping("/get-all")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> getAll(@RequestBody Pagination pagination, HttpServletRequest request) {
        return scService.getAllCenters(pagination, request);
    }

    @GetMapping("/summarized")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> getAll(HttpServletRequest request) {
        return scService.getAllCenters(request);
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> getAll(@PathVariable Integer id, HttpServletRequest request) {
        return scService.deleteCenter(id, request);
    }

}
