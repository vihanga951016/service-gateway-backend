package com.flex.service_module.api.services;

import com.flex.service_module.api.http.requests.AddCluster;
import com.flex.service_module.impl.entities.CommonHoliday;
import com.flex.service_module.impl.entities.Holiday;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface HolidayService {
    ResponseEntity<?> addHoliday(Holiday holiday, HttpServletRequest request);

    ResponseEntity<?> getHoliday(HttpServletRequest request);

    ResponseEntity<?> addCommonHoliday(CommonHoliday commonHoliday, HttpServletRequest request);

    ResponseEntity<?> commonHoliday(HttpServletRequest request);
}
