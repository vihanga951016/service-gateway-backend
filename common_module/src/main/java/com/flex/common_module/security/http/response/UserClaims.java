package com.flex.common_module.security.http.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserClaims {
    private String email;
    private Integer userId;
    private Integer center;
    private String provider;
    private Integer type;
}
