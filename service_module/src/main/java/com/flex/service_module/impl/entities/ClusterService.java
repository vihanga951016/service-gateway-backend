package com.flex.service_module.impl.entities;

import jakarta.persistence.*;
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
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cluster_services")
public class ClusterService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer orderNumber;
    @ManyToOne
    @JoinColumn(name = "cluster_id")
    private Cluster cluster;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;
}
