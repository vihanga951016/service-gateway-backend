package com.flex.service_module.api.services;

import com.flex.service_module.api.http.requests.AddCluster;
import com.flex.service_module.api.http.requests.AssignCluster;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface CSService {

    ResponseEntity<?> addCluster(AddCluster addCluster, HttpServletRequest request);

    ResponseEntity<?> updateCluster(AddCluster addCluster, HttpServletRequest request);

    ResponseEntity<?> getAllClusters(HttpServletRequest request);

    ResponseEntity<?> deleteCluster(Integer id, HttpServletRequest request);

    ResponseEntity<?> nonAssignedClusters(Integer centerId, HttpServletRequest request);

    ResponseEntity<?> assignClusterToCenter(AssignCluster assignCluster, HttpServletRequest request);

    ResponseEntity<?> getClusters(Integer centerId, HttpServletRequest request);
}
