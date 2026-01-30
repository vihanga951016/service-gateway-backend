package com.flex.service_module.impl.services.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/30/2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SPServiceHelper {
    public boolean isValidAndDifferent(String newVal, String oldVal) {
        return newVal != null &&
                !newVal.trim().isEmpty() &&
                !newVal.equals(oldVal);
    }
}
