package com.flex.service_module.impl.services;

import com.flex.common_module.http.ReturnResponse;
import com.flex.common_module.security.http.response.UserClaims;
import com.flex.common_module.security.utils.JwtUtil;
import com.flex.service_module.api.services.HolidayService;
import com.flex.service_module.impl.entities.CommonHoliday;
import com.flex.service_module.impl.entities.Holiday;
import com.flex.service_module.impl.entities.ServiceProvider;
import com.flex.service_module.impl.repositories.CommonHolidayRepository;
import com.flex.service_module.impl.repositories.HolidayRepository;
import com.flex.service_module.impl.repositories.ServiceProviderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.flex.common_module.http.ReturnResponse.*;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final CommonHolidayRepository commonHolidayRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    @Override
    public ResponseEntity<?> addHoliday(Holiday holiday, HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Provider not found");
        }

        Holiday exHoliday = holidayRepository.findByServiceProvider_IdAndHoliday(provider.getId(),
                holiday.getHoliday());

        if (exHoliday != null) {
            holidayRepository.delete(exHoliday);
        } else {
            holiday.setServiceProvider(provider);
            holidayRepository.save(holiday);
        }

        return SUCCESS("");
    }

    @Override
    public ResponseEntity<?> getHoliday(HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Provider not found");
        }

        return DATA(holidayRepository.getHolidays(provider.getId()));
    }

    @Override
    public ResponseEntity<?> addCommonHoliday(CommonHoliday commonHoliday, HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Provider not found");
        }

        CommonHoliday ex = commonHolidayRepository.findByServiceProvider_id(provider.getId());
        if (ex != null) {
            ex.setSunday(commonHoliday.isSunday());
            ex.setMonday(commonHoliday.isMonday());
            ex.setTuesday(commonHoliday.isTuesday());
            ex.setWednesday(commonHoliday.isWednesday());
            ex.setThursday(commonHoliday.isThursday());
            ex.setFriday(commonHoliday.isFriday());
            ex.setSaturday(commonHoliday.isSaturday());

            commonHolidayRepository.save(ex);

        } else {
            commonHoliday.setServiceProvider(provider);

            CommonHoliday commonHoliday1 = commonHolidayRepository.save(commonHoliday);
            log.info("commonHoliday1 has: {}", commonHoliday1);
        }
        return SUCCESS("");
    }

    @Override
    public ResponseEntity<?> commonHoliday(HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Provider not found");
        }

        return DATA(commonHolidayRepository.findByServiceProvider_id(provider.getId()));
    }
}
