package com.flex.service_module.impl.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/3/2026
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_point")
public class ServicePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String shortName;
    @ManyToOne
    @JoinColumn(name = "service_center_id")
    private ServiceCenter serviceCenter;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
    private boolean temporaryClosed;
    private boolean deleted;

    @Transient
    private Integer serviceCenterId;
    @Transient
    private String serviceCenterName;
    @Transient
    private Long serviceCount;

    public ServicePoint(Integer id, String name, String shortName, LocalTime openTime, LocalTime closeTime, boolean temporaryClosed,
                        boolean deleted, Integer serviceCenterId, String serviceCenterName, Long serviceCount) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.temporaryClosed = temporaryClosed;
        this.deleted = deleted;
        this.serviceCenterId = serviceCenterId;
        this.serviceCenterName = serviceCenterName;
        this.serviceCount = serviceCount;
    }
}
