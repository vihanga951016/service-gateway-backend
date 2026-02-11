package com.flex.service_module.impl.repositories;

import com.flex.service_module.api.http.DTO.CenterClusterData;
import com.flex.service_module.api.http.DTO.CenterClusterServiceView;
import com.flex.service_module.impl.entities.CenterCluster;
import com.flex.service_module.impl.entities.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CenterClusterRepository extends JpaRepository<CenterCluster, Integer> {

    boolean existsById(Integer id);

    CenterCluster getCenterClusterById(Integer id);

    @Query("SELECT c FROM CenterCluster c WHERE c.serviceCenter.id=:centerId AND c.cluster.id=:clusterId")
    CenterCluster centerCluster(@Param("centerId") Integer centerId, @Param("clusterId") Integer clusterId);

    @Query("SELECT c.cluster.id FROM CenterCluster c WHERE c.serviceCenter.id=:centerId")
    List<Integer> getAssignClusterIds(@Param("centerId") Integer centerId);

    @Query("SELECT c.id as id, c.cluster.name as name FROM CenterCluster c WHERE c.serviceCenter.id=:centerId")
    List<CenterClusterData> getClustersByCenterId(@Param("centerId") Integer centerId);

}
