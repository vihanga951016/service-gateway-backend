package com.flex.service_module.impl.repositories;

import com.flex.service_module.impl.entities.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClusterRepository extends JpaRepository<Cluster, Integer> {

    boolean existsByNameAndDeletedIsFalse(String name);

    boolean existsByNameAndIdNotAndDeletedIsFalse(String name, Integer id);

    Cluster findByIdAndDeletedIsFalse(Integer id);

    List<Cluster> findAllByServiceProvider_IdAndDeletedIsFalse(Integer providerId);
}
