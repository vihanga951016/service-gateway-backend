package com.flex.user_module.security;

import com.flex.common_module.constants.Colors;
import com.flex.user_module.cache.RoleCacheService;
import com.flex.user_module.exceptions.MissingRoleException;
import com.flex.user_module.impl.entities.RolePermission;
import com.flex.user_module.impl.entities.User;
import com.flex.user_module.impl.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleCacheService roleCacheService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("UserDetailsServiceImpl - loadUserByUsername");
        User user = userRepository.findByEmailAndDeletedIsFalse(username);

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getUserType() != 1) {
            if (user.getRole() == null) {
                throw new MissingRoleException("User has no designation assigned");
            }

            //for check caching
            boolean cashed = roleCacheService.isPermissionsCached(user.getId());
            log.info("permissions of the designation " + user.getRole().getId() + " is "
                    + Colors.YELLOW + (cashed ? "cashed" : "not cashed") + Colors.RESET);

            List<RolePermission> allPermissionsForDesignation = roleCacheService
                    .getAllPermissionsForRole(user.getId(), user.getRole().getId());

            authorities = allPermissionsForDesignation.stream()
                    .map(dp -> new SimpleGrantedAuthority(dp.getPermission().getPermission()))
                    .collect(Collectors.toList());

            log.info("user permissions: " + authorities);
        }

        authorities.add(new SimpleGrantedAuthority(String.valueOf(user.getUserType())));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
