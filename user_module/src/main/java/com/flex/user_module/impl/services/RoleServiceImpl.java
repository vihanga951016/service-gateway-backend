package com.flex.user_module.impl.services;

import com.flex.common_module.http.ReturnResponse;
import com.flex.common_module.security.http.response.UserClaims;
import com.flex.common_module.security.utils.JwtUtil;
import com.flex.service_module.impl.entities.ServiceProvider;
import com.flex.service_module.impl.repositories.ServiceProviderRepository;
import com.flex.user_module.api.DTO.RolePermissionView;
import com.flex.user_module.api.DTO.classes.RolePermissionsDTO;
import com.flex.user_module.api.http.requests.AddRole;
import com.flex.user_module.api.services.RoleService;
import com.flex.user_module.impl.entities.Permission;
import com.flex.user_module.impl.entities.Role;
import com.flex.user_module.impl.entities.RolePermission;
import com.flex.user_module.impl.entities.User;
import com.flex.user_module.impl.repositories.PermissionRepository;
import com.flex.user_module.impl.repositories.RolePermissionRepository;
import com.flex.user_module.impl.repositories.RoleRepository;
import com.flex.user_module.impl.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.flex.common_module.http.ReturnResponse.*;
/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/15/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public ResponseEntity<?> systemPermissions(HttpServletRequest request) {
        log.info(request.getRequestURI());
        return DATA(permissionRepository.getPermissions());
    }

    @Override
    public ResponseEntity<?> addRole(AddRole addRole, HttpServletRequest request) {
        log.info(request.getRequestURI(), "{} body {}" ,addRole);

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("Not allowed for this action");
        }

        ServiceProvider serviceProvider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (serviceProvider == null) {
            return CONFLICT("You have no access to this service");
        }

        if (roleRepository.existsByRoleAndDeletedIsFalse(addRole.getRoleName())) {
            return CONFLICT("Role already exists");
        }

        if (addRole.getRoleName() == null || addRole.getRoleName().isEmpty()) {
            return BAD_REQUEST("Nothing to add");
        }

        if (addRole.getPermissions() == null || addRole.getPermissions().isEmpty()) {
            return BAD_REQUEST("Nothing to add");
        }

        List<Permission> permissions = permissionRepository.getAlPermissionsByPermission(
                addRole.getPermissions()
        );

        if (permissions == null || permissions.isEmpty()) {
            return CONFLICT("No permissions found");
        }

        Role role = Role.builder()
                .role(addRole.getRoleName())
                .serviceProvider(serviceProvider)
                .build();

        List<RolePermission> rolePermissions = permissions.stream().map(
                permission -> RolePermission.builder().role(role).permission(permission).build()
        ).toList();

        roleRepository.save(role);
        rolePermissionRepository.saveAll(rolePermissions);

        return SUCCESS("Role added");
    }

    @Override
    public ResponseEntity<?> updateRole(AddRole addRole, HttpServletRequest request) {
        log.info(request.getRequestURI(), "{} body {}" ,addRole);

        if (addRole.getRoleId() == null) {
            return BAD_REQUEST("Role id is not found");
        }

        if (addRole.getPermissions() == null || addRole.getPermissions().isEmpty()) {
            return BAD_REQUEST("Permissions can not be empty");
        }

        Role role = roleRepository.findByIdAndDeletedIsFalse(addRole.getRoleId());

        if (role == null) {
            return CONFLICT("Role not found");
        }

        boolean roleUpdated = false;
        boolean permissionsUpdated = false;

        if (addRole.getRoleName() != null
                && !addRole.getRoleName().isEmpty()
                && !role.getRole().equals(addRole.getRoleName())) {
            role.setRole(addRole.getRoleName());

            roleRepository.save(role);

            roleUpdated = true;
        }

        List<String> rolePermissions = rolePermissionRepository.getRolePermissions(addRole.getRoleId());

        boolean isEqual = new HashSet<>(rolePermissions).equals(new HashSet<>(addRole.getPermissions()));

        if (!isEqual) {
            List<RolePermission> deletingRolePermissions = rolePermissionRepository
                    .getAllRolePermissions(addRole.getRoleId());

            rolePermissionRepository.deleteAll(deletingRolePermissions);

            List<Permission> permissions = permissionRepository.getAlPermissionsByPermission(
                    addRole.getPermissions()
            );

            List<RolePermission> newRolePermissions = permissions.stream().map(
                    permission -> RolePermission.builder().role(role).permission(permission).build()
            ).toList();

            rolePermissionRepository.saveAll(newRolePermissions);

            permissionsUpdated = true;
        }

        if (roleUpdated && permissionsUpdated) {
            return SUCCESS("Role and permissions updated");
        }

        if (roleUpdated) {
            return SUCCESS("Role updated");
        }

        if (permissionsUpdated) {
            return SUCCESS("Permissions updated");
        }

        return SUCCESS("Nothing to update");

    }

    @Override
    public ResponseEntity<?> deleteRole(Integer id, HttpServletRequest request) {
        log.info(request.getRequestURI(), "{} id {}" ,id);

        if (id == null) {
            return BAD_REQUEST("Role id is not found");
        }

        Role role = roleRepository.findByIdAndDeletedIsFalse(id);

        if (role == null) {
            return CONFLICT("Role not found");
        }

        role.setDeleted(true);

        roleRepository.save(role);

        List<RolePermission> rolePermissions = rolePermissionRepository
                .getAllRolePermissions(id);

        rolePermissionRepository.deleteAll(rolePermissions);

        return SUCCESS("Role deleted");
    }

    @Override
    public ResponseEntity<?> getAllRoles(HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("Not allowed for this action");
        }

        ServiceProvider serviceProvider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (serviceProvider == null) {
            return CONFLICT("You have no access to this service");
        }

        List<RolePermissionView> rolePermissionViews = roleRepository.findRolesWithPermissions(serviceProvider.getId());

        Map<Integer, RolePermissionsDTO> roleMap = new LinkedHashMap<>();

        for (RolePermissionView row : rolePermissionViews) {
            roleMap.computeIfAbsent(
                    row.getRoleId(),
                    roleId -> new RolePermissionsDTO(roleId, row.getRoleName(), new ArrayList<>())
            );

            roleMap.get(row.getRoleId())
                    .getPermissions()
                    .add(row.getPermissionName());
        }

        return DATA(new ArrayList<>(roleMap.values()));
    }

    @Override
    public ResponseEntity<?> getRoleById(Integer id, HttpServletRequest request) {
        log.info(request.getRequestURI(), "{} id {}" ,id);

        Role role = roleRepository.findByIdAndDeletedIsFalse(id);

        if (role == null) {
            return CONFLICT("Role not found");
        }

        List<String> rolePermissions = rolePermissionRepository.getRolePermissions(id);

        RolePermissionsDTO rolePermissionsDTO = RolePermissionsDTO.builder()
                .id(role.getId())
                .name(role.getRole())
                .permissions(rolePermissions)
                .build();

        return DATA(rolePermissionsDTO);
    }
}
