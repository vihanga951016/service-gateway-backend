package com.flex.service_module.impl.repositories;

import com.flex.service_module.api.http.DTO.BestServicePointForJob;
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

    @Query(value = """
            SELECT sp.id AS id,
                   sp.name AS name,
                   SEC_TO_TIME(SUM(TIME_TO_SEC(jp.end_time) - TIME_TO_SEC(jp.start_time))) AS totalJobTime
            FROM service_point sp
                     LEFT JOIN available_services av 
                            ON av.service_point_id = sp.id
                     LEFT JOIN jobs_at_point jp 
                            ON jp.service_point_id = sp.id
            WHERE sp.service_center_id = :serviceCenterId
              AND av.service_id = :serviceId
              AND sp.deleted = false
            GROUP BY sp.id
            ORDER BY 
                (SUM(TIME_TO_SEC(jp.end_time) - TIME_TO_SEC(jp.start_time)) IS NOT NULL),
                SUM(TIME_TO_SEC(jp.end_time) - TIME_TO_SEC(jp.start_time)) ASC
            LIMIT 1
            """,
            nativeQuery = true)
    BestServicePointForJob findBestServicePoint(
            @Param("serviceCenterId") Integer serviceCenterId,
            @Param("serviceId") Integer serviceId);
}
