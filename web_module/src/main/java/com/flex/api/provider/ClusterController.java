package com.flex.api.provider;

import com.flex.service_module.api.http.requests.AddCluster;
import com.flex.service_module.api.services.CSService;
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
@RequestMapping("/clusters")
@RequiredArgsConstructor
public class ClusterController {

    private final CSService clusterService;

    @PostMapping("/add")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CS)")
    public ResponseEntity<?> add(@RequestBody AddCluster addCluster, HttpServletRequest request) {
        return clusterService.addCluster(addCluster, request);
    }

    @PostMapping("/update")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CS)")
    public ResponseEntity<?> update(@RequestBody AddCluster addCluster, HttpServletRequest request) {
        return clusterService.updateCluster(addCluster, request);
    }

    @GetMapping("/get-all")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> getAll(HttpServletRequest request) {
        return clusterService.getAllClusters(request);
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CS)")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        return clusterService.deleteCluster(id, request);
    }
}
