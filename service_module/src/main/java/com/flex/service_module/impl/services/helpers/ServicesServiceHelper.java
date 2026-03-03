package com.flex.service_module.impl.services.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/2/2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServicesServiceHelper {

    public String formatDuration(LocalTime time) {
        if (time == null) return null;

        int hours = time.getHour();
        int minutes = time.getMinute();

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append("h");
        }

        if (minutes > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(minutes).append("min");
        }

        return sb.length() == 0 ? "0min" : sb.toString();
    }
}
