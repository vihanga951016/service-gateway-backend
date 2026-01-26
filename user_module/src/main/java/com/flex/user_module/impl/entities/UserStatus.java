package com.flex.user_module.impl.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/25/2026
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_status")
public class UserStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    @JoinColumn(name = "userId")
    private User user;
    private boolean providerApproved;
}
