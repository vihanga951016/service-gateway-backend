package com.flex.service_module.api.services;

import com.flex.common_module.http.pagination.Pagination;
import com.flex.service_module.impl.entities.ServiceCenter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/1/2026
 */
public interface SCService {

    ResponseEntity<?> addNewCenter(ServiceCenter serviceCenter, HttpServletRequest request);

    ResponseEntity<?> updateCenter(ServiceCenter serviceCenter, HttpServletRequest request);

    ResponseEntity<?> getAllCenters(Pagination pagination, HttpServletRequest request);

    ResponseEntity<?> getAllCenters(HttpServletRequest request);

    ResponseEntity<?> getCenter(Integer id, HttpServletRequest request);

    ResponseEntity<?> getAllForDropdown(HttpServletRequest request);

    ResponseEntity<?> deleteCenter(Integer id, HttpServletRequest request);
}
