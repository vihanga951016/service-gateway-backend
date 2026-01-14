package com.flex.user_module.impl.repositories;

import com.flex.user_module.impl.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByEmailAndDeletedIsFalse(String email);

    User findByEmailAndDeletedIsFalse(String email);

}
