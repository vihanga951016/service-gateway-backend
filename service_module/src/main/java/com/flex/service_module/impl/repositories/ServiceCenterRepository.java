package com.flex.service_module.impl.repositories;

import com.flex.service_module.api.http.DTO.ServiceCenterStatusView;
import com.flex.service_module.impl.entities.ServiceCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Integer> {

    boolean existsByIdAndDeletedIsFalse(Integer id);

    boolean existsByNameAndServiceProvider_IdAndDeletedIsFalse(String name, Integer SPId);

    boolean existsByNameIgnoreCaseAndServiceProvider_IdAndDeletedIsFalseAndIdNot(String name, Integer SPId, Integer id);

    boolean existsByContactIgnoreCaseAndServiceProvider_IdAndDeletedIsFalseAndIdNot(String contact, Integer SPId, Integer id);

    ServiceCenter findByIdAndDeletedIsFalse(Integer id);

    Page<ServiceCenter> findByServiceProvider_IdAndDeletedFalse(
            Integer providerId,
            Pageable pageable
    );

    @Query(
            "SELECT sc FROM ServiceCenter sc " +
                    "WHERE sc.serviceProvider.id = :providerId " +
                    "AND sc.deleted = false " +
                    "AND (LOWER(sc.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR LOWER(sc.contact) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<ServiceCenter> searchCenters(
            @Param("providerId") Integer providerId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(
            value = "SELECT c.id AS id, " +
                    "c.name AS name, " +
                    "c.contact AS contact, " +
                    "c.location AS location, " +
                    "CASE WHEN :time BETWEEN c.open_time AND c.close_time " +
                    "THEN 'Opened' ELSE 'Closed' END AS status " +
                    "FROM service_centers c " +
                    "WHERE c.service_provider_id = :providerId " +
                    "AND c.deleted = false",
            nativeQuery = true
    )
    List<ServiceCenterStatusView> findCentersWithStatus(
            @Param("providerId") Integer providerId,
            @Param("time") LocalTime time
    );

}

