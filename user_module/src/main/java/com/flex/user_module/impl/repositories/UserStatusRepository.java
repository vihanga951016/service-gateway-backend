package com.flex.user_module.impl.repositories;

import com.flex.user_module.impl.entities.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/25/2026
 */
public interface UserStatusRepository extends JpaRepository<UserStatus, Integer> {
    UserStatus findByUserIdAndProviderApprovedIsFalse(Integer userId);
}
