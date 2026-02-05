package com.flex.service_module.api.services;

import com.flex.common_module.http.pagination.Pagination;
import com.flex.service_module.api.http.requests.AssignServiceToPoint;
import com.flex.service_module.impl.entities.Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ServicesService {

    ResponseEntity<?> addService(Service service, HttpServletRequest request);

    ResponseEntity<?> updateService(Service service, HttpServletRequest request);

    ResponseEntity<?> availableServicesForPoint(Integer servicePoint, HttpServletRequest request);

    ResponseEntity<?> assignedServicesForPoint(Integer servicePoint, HttpServletRequest request);

    ResponseEntity<?> assignedPointsForService(Integer service, HttpServletRequest request);

    ResponseEntity<?> assignServicesForPoint(AssignServiceToPoint assignServiceToPoint, HttpServletRequest request);

    ResponseEntity<?> removeServicesFromPoint(AssignServiceToPoint assignServiceToPoint, HttpServletRequest request);

    ResponseEntity<?> getAllServices(Pagination pagination, HttpServletRequest request);

    ResponseEntity<?> deleteService(Integer id, HttpServletRequest request);

}
