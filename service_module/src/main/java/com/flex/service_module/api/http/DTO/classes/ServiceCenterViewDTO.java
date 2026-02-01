package com.flex.service_module.api.http.DTO.classes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/1/2026
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCenterViewDTO {
    private Integer id;
    private String name;
    private String location;
    private String contact;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String fOpenTime;
    private String fCloseTime;
}
