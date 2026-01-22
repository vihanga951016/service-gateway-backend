package com.flex.user_module.impl.services;

import com.flex.common_module.security.utils.HashUtil;
import com.flex.common_module.security.utils.JwtUtil;
import com.flex.service_module.impl.entities.ServiceCenter;
import com.flex.service_module.impl.entities.ServiceProvider;
import com.flex.service_module.impl.repositories.ServiceCenterRepository;
import com.flex.service_module.impl.repositories.ServiceProviderRepository;
import com.flex.user_module.api.http.requests.Login;
import com.flex.user_module.api.http.requests.Register;
import com.flex.user_module.api.http.responses.LoginResponse;
import com.flex.user_module.api.services.UserService;
import com.flex.user_module.impl.entities.*;
import com.flex.user_module.impl.repositories.*;
import com.flex.user_module.impl.services.helpers.UserServiceHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.flex.common_module.http.ReturnResponse.*;
import static com.flex.user_module.constants.UserConstant.*;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class UserServiceImpl implements UserService {

    private final JwtUtil jwtUtil;

    private final UserServiceHelper userServiceHelper;

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final UserRepository userRepository;
    private final UserLoginRepository userLoginRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public ResponseEntity<?> register(Register register, HttpServletRequest request) {
        log.info(request.getRequestURI(), "{} body - {}", register);

        if (serviceProviderRepository.existsByNameAndEmailAndDeletedIsFalse(
                register.getProvider(), register.getProviderEmail()
        )) {
            return CONFLICT("Service provider already registered");
        }

        if (userRepository.existsByEmailAndDeletedIsFalse(register.getAdminEmail())) {
            return CONFLICT("User already registered");
        }

        //create service provider entity
        ServiceProvider serviceProvider = ServiceProvider.builder()
                .name(register.getProvider())
                .email(register.getProviderEmail())
                .contact(register.getContact())
                .build();

        ServiceProvider savedSP = serviceProviderRepository.save(serviceProvider);

        //create service center entity if has no other centers
        if (!register.isHasMultipleBranches()) {
            ServiceCenter serviceCenter = ServiceCenter.builder()
                    .name(serviceProvider.getName())
                    .build();

            serviceCenterRepository.save(serviceCenter);
        }

        //create admin entity
        Role admin = Role.builder().
                role("Admin")
                .serviceProvider(savedSP)
                .build();

        Role savedAdmin = roleRepository.save(admin);

        //create permissions for admin
        List<Permission> permissions = permissionRepository.findAll();

        List<RolePermission> rolePermissions = permissions.stream().map(
                p -> RolePermission.builder().role(savedAdmin).permission(p).build()
        ).collect(Collectors.toList());

        rolePermissionRepository.saveAll(rolePermissions);

        //create user entity for admin
        User user = User.builder()
                .fName(register.getAdminFName())
                .lName(register.getAdminLName())
                .email(register.getAdminEmail())
                .userType(ADMIN)
                .password(HashUtil.hash(register.getAdminPassword()))
                .build();

        userRepository.save(user);

        return SUCCESS("Registration Completed. Please login");
    }

    //todo create a service for customer registration - google OAuth

    @Override
    public ResponseEntity<?> login(Login login, HttpServletRequest request) {
        log.info(request.getRequestURI(), "{} body - {}", login);

        User user = userRepository.findByEmailAndDeletedIsFalse(login.getUsername());

        if (user == null) {
            return CONFLICT("Invalid username");
        }

        if (!HashUtil.checkEncrypted(login.getPassword(), user.getPassword())) {
            return CONFLICT("Invalid password");
        }

        if (user.getRole() == null) {
            return CONFLICT("Invalid role");
        }

        //if has any previous login with no logout(logged in and close the browser without logout)
        // , logout from every login
        userServiceHelper.logoutFromPreviousLogins(user.getId());

        Map<String, Object> claims = new HashMap<>();

        claims.put("user", user.getId());
        claims.put("type", user.getUserType());
        claims.put("center", user.getServiceCenter() != null ? user.getServiceCenter().getId() : null);

        String token = jwtUtil.generateToken(claims, user.getEmail());
        String refreshToken = jwtUtil.refreshToken(claims, user.getEmail());

        UserLogin userLogin = UserLogin.builder()
                .loginTime(new Date())
                .token(token)
                .userId(user.getId())
                .build();

        userLoginRepository.save(userLogin);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();

        return DATA(response);
    }
}
