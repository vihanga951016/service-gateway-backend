package com.flex.service_module.impl.repositories;

import com.flex.service_module.api.http.DTO.CenterClusterServicesData;
import com.flex.service_module.impl.entities.CenterClusterServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CCSRepository extends JpaRepository<CenterClusterServices, Integer> {

    CenterClusterServices findCenterClusterServicesById(Integer id);

    List<CenterClusterServices> findAllByCenterCluster_id(Integer id);

    @Query("SELECT c.id as id, c.service.name as service, c.total as total, c.downPay as downPay, c.orderNumber as orderNumber, " +
            "c.serviceTime as serviceTime, c.disabled as disabled FROM CenterClusterServices c WHERE c.centerCluster.id=:ccId ORDER BY c.orderNumber ASC")
    List<CenterClusterServicesData> centerClusterServicesData(@Param("ccId") Integer ccId);
}
