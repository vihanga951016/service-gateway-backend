package com.flex.user_module.api.http.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Register {
    private String provider;
    private String providerEmail;
    private String contact;
    private String adminFName;
    private String adminLName;
    private String adminEmail;
    private String adminPassword;
}
