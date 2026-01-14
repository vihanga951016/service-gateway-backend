package com.flex.common_module.security.impls.entities;

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
@Table(name = "expired_tokens", indexes = {
        @Index(name = "idx_user_id", columnList = "userId")
})
public class ExpiredToken {
    @Id
    @Column(length = 600)
    private String id;
    private Integer userId;
}
