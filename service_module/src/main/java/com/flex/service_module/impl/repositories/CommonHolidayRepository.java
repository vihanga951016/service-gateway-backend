package com.flex.service_module.impl.repositories;

import com.flex.service_module.impl.entities.CommonHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonHolidayRepository extends JpaRepository<CommonHoliday, Integer> {
    CommonHoliday findByServiceProvider_id(Integer serviceProviderId);
}
