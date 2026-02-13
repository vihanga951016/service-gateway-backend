package com.flex.service_module.impl.repositories;

import com.flex.service_module.impl.entities.ServicePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServicePointRepository extends JpaRepository<ServicePoint, Integer> {
    boolean existsByNameAndDeletedIsFalse(String name);

    boolean existsByNameAndIdNotAndDeletedIsFalse(String name, Integer id);

    ServicePoint findByIdAndDeletedIsFalse(Integer id);

    @Query("SELECT new ServicePoint(s.id, s.name, s.shortName, s.openTime, s.closeTime, s.temporaryClosed, s.deleted, " +
            "s.serviceCenter.id, s.serviceCenter.name, COUNT(av.service.id)) " +
            "FROM ServicePoint s " +
            "LEFT JOIN AvailableService av ON av.servicePoint.id = s.id " +
            "WHERE s.serviceCenter.id=:serviceCenterId and s.deleted is false " +
            "GROUP BY " +
            "        s.id, s.name, s.shortName, " +
            "        s.openTime, s.closeTime, " +
            "        s.temporaryClosed, s.deleted, " +
            "        s.id, s.name")
    List<ServicePoint> servicePointsByCenter(@Param("serviceCenterId") Integer serviceCenterId);

    @Query("SELECT s.id FROM ServicePoint s WHERE s.serviceCenter.id=:serviceCenterId and s.deleted is false")
    List<Integer> servicePointIdsByCenter(@Param("serviceCenterId") Integer serviceCenterId);
}
