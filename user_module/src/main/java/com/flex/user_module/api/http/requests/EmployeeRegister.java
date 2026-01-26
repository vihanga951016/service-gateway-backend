package com.flex.user_module.api.http.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/25/2026
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRegister {
    private String fName;
    private String lName;
    private String contact;
    private String nic;
    private String email;
    private String providerId;
    private String profileImage;
}
