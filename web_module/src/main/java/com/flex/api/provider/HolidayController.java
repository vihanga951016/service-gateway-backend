package com.flex.api.provider;

import com.flex.service_module.api.services.HolidayService;
import com.flex.service_module.impl.entities.CommonHoliday;
import com.flex.service_module.impl.entities.Holiday;
import com.flex.service_module.impl.entities.ServiceCenter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 3/9/2026
 */
@RestController
@RequestMapping("/holiday")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping("/add")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).HM)")
    public ResponseEntity<?> add(@RequestBody Holiday holiday, HttpServletRequest request) {
        return holidayService.addHoliday(holiday, request);
    }

    @GetMapping("/get-all")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> getAll(HttpServletRequest request) {
        return holidayService.getHoliday(request);
    }

    @PostMapping("/common/add")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).HM)")
    public ResponseEntity<?> addCommonHolidays(@RequestBody CommonHoliday commonHoliday, HttpServletRequest request) {
        return holidayService.addCommonHoliday(commonHoliday, request);
    }

    @GetMapping("/common")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> commonHolidays(HttpServletRequest request) {
        return holidayService.commonHoliday(request);
    }
}
