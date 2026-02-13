package com.flex.job_module.impl.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flex.service_module.impl.entities.Service;
import com.flex.service_module.impl.entities.ServicePoint;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/12/2026
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "jobs_at_point")
public class JobAtPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "service_point_id")
    private ServicePoint servicePoint;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;
    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @JsonFormat(pattern = "HH:mm:ss")
    @Column(nullable = false)
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(nullable = false)
    private LocalTime endTime;

    private int status;

    private boolean dummyEntity;
}
