package com.flex.service_module.impl.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_providers")
public class ServiceProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true, nullable = false, length = 8)
    private String providerId;
    private String name;
    private String email;
    private String contact;
    private boolean hasMultipleCenters;
    private boolean restricted;
    private boolean deleted;
}
