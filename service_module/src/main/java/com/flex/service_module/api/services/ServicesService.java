package com.flex.service_module.api.services;

import com.flex.common_module.http.pagination.Pagination;
import com.flex.service_module.api.http.requests.AssignServiceToPoint;
import com.flex.service_module.impl.entities.Service;
import com.flex.service_module.impl.entities.ServiceCenter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ServicesService {

    ResponseEntity<?> addService(Service service, HttpServletRequest request);

    ResponseEntity<?> updateService(Service service, HttpServletRequest request);

    ResponseEntity<?> getService(Integer id, HttpServletRequest request);

    ResponseEntity<?> getAllServices(Pagination pagination, HttpServletRequest request);

    ResponseEntity<?> nonAssignedServicesForPoint(Integer pointId, HttpServletRequest request);

    ResponseEntity<?> deleteService(Integer id, HttpServletRequest request);

    ResponseEntity<?> assignServicesToPoint(AssignServiceToPoint assignServiceToPoint, HttpServletRequest request);
}
