package com.flex.job_module.impl.entities;
import com.flex.service_module.impl.entities.AvailableService;
import com.flex.service_module.impl.entities.CenterClusterServices;
import com.flex.service_module.impl.entities.Service;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/11/2026
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_services")
public class JobService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "center_cluster_service_id")
    private CenterClusterServices centerClusterService;

}
