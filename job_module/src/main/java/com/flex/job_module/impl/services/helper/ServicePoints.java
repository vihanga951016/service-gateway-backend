package com.flex.job_module.impl.services.helper;

import com.flex.service_module.impl.entities.ServicePoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ServicePoints {

    public List<Integer> findEmptyPoints(List<ServicePoint> servicePointList) {
        return new ArrayList<>(
                servicePointList.stream()
                        .map(ServicePoint::getId)
                        .toList()
        );
    }
}
