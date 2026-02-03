package com.flex.service_module.impl.services.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    public String formatDuration(Date time) {

        if (time == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(time);

        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append("h");
        }

        if (minutes > 0) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(minutes).append("min");
        }

        return sb.isEmpty() ? "0min" : sb.toString();
    }
}
