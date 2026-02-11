package com.flex.service_module.api.services;

import com.flex.service_module.api.http.requests.AddCluster;
import com.flex.service_module.api.http.requests.AssignCluster;
import com.flex.service_module.api.http.requests.UpdateCenterClusterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CSService {

    ResponseEntity<?> addCluster(AddCluster addCluster, HttpServletRequest request);

    ResponseEntity<?> updateCluster(AddCluster addCluster, HttpServletRequest request);

    ResponseEntity<?> getAllClusters(HttpServletRequest request);

    ResponseEntity<?> deleteCluster(Integer id, HttpServletRequest request);

    ResponseEntity<?> nonAssignedClusters(Integer centerId, HttpServletRequest request);

    ResponseEntity<?> assignClusterToCenter(AssignCluster assignCluster, HttpServletRequest request);

    ResponseEntity<?> getClusters(Integer centerId, HttpServletRequest request);

    ResponseEntity<?> removeClusterFromCenter(Integer centerId, HttpServletRequest request);

    ResponseEntity<?> disableCCServices(Integer ccServiceId, HttpServletRequest request);

    ResponseEntity<?> getAllCenterClusterServices(Integer centerClusterId, HttpServletRequest request);

    ResponseEntity<?> updateCenterClusterService(UpdateCenterClusterService updateClusterService, HttpServletRequest request);

    ResponseEntity<?> reOrderServices(Integer ccId, List<UpdateCenterClusterService> serviceList, HttpServletRequest request);

    //mainly check cluster services which assigned to center, are already mapped with a service point in that service center
    ResponseEntity<?> clusterServicesMapCheck(Integer ccId, HttpServletRequest request);
}
