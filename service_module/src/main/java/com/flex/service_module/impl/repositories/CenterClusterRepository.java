package com.flex.service_module.impl.repositories;

import com.flex.service_module.impl.entities.CenterCluster;
import com.flex.service_module.impl.entities.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CenterClusterRepository extends JpaRepository<CenterCluster, Integer> {

    @Query("SELECT c FROM CenterCluster c WHERE c.serviceCenter=:centerId AND c.cluster.id=:clusterId")
    CenterCluster centerCluster(@Param("centerId") Integer centerId, @Param("clusterId") Integer clusterId);

    @Query("SELECT c.cluster.id FROM CenterCluster c WHERE c.serviceCenter.id=:centerId")
    List<Integer> getAssignClusterIds(@Param("centerId") Integer centerId);
}
