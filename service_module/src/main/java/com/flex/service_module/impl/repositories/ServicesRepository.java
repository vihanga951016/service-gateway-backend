package com.flex.service_module.impl.repositories;

import com.flex.service_module.impl.entities.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServicesRepository extends JpaRepository<Service, Integer> {
    boolean existsByNameAndProvider_IdAndDeletedIsFalse(String name, Integer providerId);

    boolean existsByNameAndProvider_IdAndDeletedIsFalseAndIdNot(
            String name, Integer providerId, Integer id
    );

    Service findByIdAndDeletedIsFalse(Integer id);

    List<Service> findAllByProvider_IdAndDeletedIsFalse(Integer providerId);

    @Query(
            "SELECT s FROM Service s " +
                    "WHERE s.provider.id = :providerId " +
                    "AND s.deleted = false " +
                    "AND ( " +
                    "     :searchText IS NULL " +
                    "     OR :searchText = '' " +
                    "     OR LOWER(s.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
                    ")"
    )
    Page<Service> findAllWithSearch(
            @Param("providerId") Integer providerId,
            @Param("searchText") String searchText,
            Pageable pageable
    );
}
