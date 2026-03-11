package com.flex.service_module.impl.repositories;

import com.flex.service_module.api.http.DTO.Holidays;
import com.flex.service_module.impl.entities.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Integer> {


    Holiday findByServiceProvider_IdAndHoliday(Integer serviceProviderId, LocalDate holiday);

    @Query("SELECT h.name as name, h.holiday as holiday FROM Holiday h WHERE h.serviceProvider.id=:serviceProviderId")
    List<Holidays> getHolidays(@Param("serviceProviderId") Integer serviceProviderId);
}
