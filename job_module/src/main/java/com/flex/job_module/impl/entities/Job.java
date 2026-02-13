package com.flex.job_module.impl.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flex.service_module.impl.entities.ServiceCenter;
import com.flex.service_module.impl.entities.ServicePoint;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

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
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // foreign keys
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // times
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime estimatedTotalTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate appointmentDate;
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(nullable = false)
    private LocalTime appointmentTime;
    private Integer totalPrice;
    private Integer downPayment;

    private int status;
    private boolean dummy;
    private boolean paymentVerified;
}
