package com.flex.service_module.impl.repositories;

import com.flex.service_module.impl.entities.ClusterService;
import com.flex.service_module.impl.entities.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClusterServiceRepository extends JpaRepository<ClusterService, Integer> {

    List<ClusterService> findAllByCluster_IdOrderById(Integer clusterId);
}
