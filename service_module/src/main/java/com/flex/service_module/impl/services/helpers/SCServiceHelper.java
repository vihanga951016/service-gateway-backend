package com.flex.service_module.impl.services.helpers;

import com.flex.service_module.impl.entities.ServiceCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/1/2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SCServiceHelper {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("hh:mm a");

    public String validateServiceCenter(ServiceCenter sc) {

        if (sc == null) {
            return "ServiceCenter object is null";
        }

        if (sc.getName() == null || sc.getName().trim().isEmpty()) {
            return "Name is required";
        }

        if (sc.getContact() == null || sc.getContact().trim().isEmpty()) {
            return "Contact is required";
        }

        if (sc.getLocation() == null || sc.getLocation().trim().isEmpty()) {
            return "Location is required";
        }

        if (sc.getOpenTime() == null) {
            return "Open time is required";
        }

        if (sc.getCloseTime() == null) {
            return "Close time is required";
        }

        return "fine";
    }

    public String formatTimeRange(LocalTime time) {

        if (time == null) return "";

        return time.format(TIME_FMT);
    }

}
