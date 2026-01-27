package com.flex.user_module.api.services;

import com.flex.user_module.api.http.requests.AddRole;
import com.flex.user_module.impl.entities.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/15/2026
 */
public interface RoleService {

    ResponseEntity<?> systemPermissions(HttpServletRequest request);

    ResponseEntity<?> addRole(AddRole addRole, HttpServletRequest request);

    ResponseEntity<?> updateRole(AddRole addRole, HttpServletRequest request);

    ResponseEntity<?> deleteRole(Integer id, HttpServletRequest request);

    ResponseEntity<?> getAllRoles(HttpServletRequest request);

    ResponseEntity<?> getRoleById(Integer id, HttpServletRequest request);
}
