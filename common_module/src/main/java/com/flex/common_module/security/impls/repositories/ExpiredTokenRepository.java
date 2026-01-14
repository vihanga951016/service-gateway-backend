package com.flex.common_module.security.impls.repositories;

import com.flex.common_module.security.impls.entities.ExpiredToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
public interface ExpiredTokenRepository extends JpaRepository<ExpiredToken, String> {
    boolean existsById(String token);
}
