package com.flex.service_module.impl.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/2/2026
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "services")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "service_provider_id")
    private ServiceProvider provider;
    @JsonFormat(pattern = "HH:mm:ss", timezone = "Asia/Colombo")
    @Temporal(TemporalType.TIME)
    private Date serviceTime;
    private boolean serviceTimeDepends;
    @Column(length = 512)
    private String description;
    private Integer totalPrice;
    private boolean totalPriceDepends;
    private Integer downPrice;
    private boolean deleted;

    public Service(Integer id) {
        this.id = id;
    }
}
