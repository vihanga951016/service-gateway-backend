package com.flex.user_module.impl.repositories;

import com.flex.user_module.impl.entities.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/15/2026
 */
public interface UserLoginRepository extends JpaRepository<UserLogin, Integer> {

    @Query("SELECT l FROM UserLogin l WHERE l.userId=:userId AND l.logout = false")
    List<UserLogin> getAllLogin(Integer userId);
}
