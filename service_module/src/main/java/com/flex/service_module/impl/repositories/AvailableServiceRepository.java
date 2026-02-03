package com.flex.service_module.impl.repositories;

import com.flex.service_module.impl.entities.AvailableService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvailableServiceRepository extends JpaRepository<AvailableService,Integer> {

    @Query("SELECT new AvailableService(s.id, s.servicePoint.id, s.service.id) FROM AvailableService s " +
            "WHERE s.service.id=:serviceId AND s.servicePoint.id=:pointId")
    AvailableService availableService(@Param("serviceId") Integer serviceId, @Param("pointId") Integer pointId);

    @Query("SELECT a.service.id FROM AvailableService a WHERE a.servicePoint.id=:providerId")
    List<Integer> availableServicesIds(@Param("providerId") Integer providerId);
}
