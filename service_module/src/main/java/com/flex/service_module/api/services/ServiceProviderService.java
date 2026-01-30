package com.flex.service_module.api.services;

import com.flex.service_module.impl.entities.ServiceProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ServiceProviderService {

    ResponseEntity<?> editServiceProvider(ServiceProvider serviceProvider, HttpServletRequest request);
}
