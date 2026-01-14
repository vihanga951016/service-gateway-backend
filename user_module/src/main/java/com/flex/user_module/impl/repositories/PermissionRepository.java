package com.flex.user_module.impl.repositories;

import com.flex.user_module.impl.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
}
