package com.flex.api.provider;

import com.flex.service_module.api.http.requests.AddCluster;
import com.flex.service_module.api.http.requests.AssignCluster;
import com.flex.service_module.api.http.requests.UpdateCenterClusterService;
import com.flex.service_module.api.services.CSService;
import com.flex.service_module.impl.entities.Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/non-assign-to/center/{centerId}")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> nonAssignClusters(@PathVariable("centerId") Integer centerId, HttpServletRequest request) {
        return clusterService.nonAssignedClusters(centerId, request);
    }

    @PostMapping("/assign-to-center")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> nonAssignClusters(@RequestBody AssignCluster assignCluster, HttpServletRequest request) {
        return clusterService.assignClusterToCenter(assignCluster, request);
    }

    @GetMapping("/get-all/center/{centerId}")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> getClusters(@PathVariable("centerId") Integer centerId, HttpServletRequest request) {
        return clusterService.getClusters(centerId, request);
    }

    @DeleteMapping("/remove/center-cluster/{ccId}")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> ccId(@PathVariable("ccId") Integer ccId, HttpServletRequest request) {
        return clusterService.removeClusterFromCenter(ccId, request);
    }

    @PutMapping("/center-cluster-service-disable/{ccServiceId}")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> disability(@PathVariable("ccServiceId") Integer ccServiceId, HttpServletRequest request) {
        return clusterService.disableCCServices(ccServiceId, request);
    }

    @GetMapping("/get-all-center-cluster-service/center-cluster/{ccId}")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> centerClusterServices(@PathVariable("ccId") Integer ccId, HttpServletRequest request) {
        return clusterService.getAllCenterClusterServices(ccId, request);
    }

    @PostMapping("/update-center-cluster-service")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> updateCenterClusterService(@RequestBody UpdateCenterClusterService updateCenterClusterService,
                                                        HttpServletRequest request) {
        return clusterService.updateCenterClusterService(updateCenterClusterService, request);
    }

    @PostMapping("/re-order-services/center-cluster/{ccId}")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> reOrderClusterService(@PathVariable Integer ccId, @RequestBody List<UpdateCenterClusterService> updateCenterClusterService,
                                                        HttpServletRequest request) {
        return clusterService.reOrderServices(ccId, updateCenterClusterService, request);
    }

    @GetMapping("/check-services-assign-to-point/{ccId}")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).CM)")
    public ResponseEntity<?> clusterServicesMapCheck(@PathVariable Integer ccId, HttpServletRequest request) {
        return clusterService.clusterServicesMapCheck(ccId, request);
    }
}
