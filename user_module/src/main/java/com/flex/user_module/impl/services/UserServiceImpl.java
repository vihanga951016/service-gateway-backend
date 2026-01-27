package com.flex.user_module.impl.services;

import com.flex.common_module.security.http.response.UserClaims;
import com.flex.common_module.security.utils.CryptoUtil;
import com.flex.common_module.security.utils.HashUtil;
import com.flex.common_module.security.utils.JwtUtil;
import com.flex.service_module.impl.entities.ServiceCenter;
import com.flex.service_module.impl.entities.ServiceProvider;
import com.flex.service_module.impl.repositories.ServiceCenterRepository;
import com.flex.service_module.impl.repositories.ServiceProviderRepository;
import com.flex.user_module.api.http.requests.EmployeeRegister;
import com.flex.user_module.api.http.requests.Login;
import com.flex.user_module.api.http.requests.Register;
import com.flex.user_module.api.http.responses.HeaderData;
import com.flex.user_module.api.http.responses.LoginResponse;
import com.flex.user_module.api.services.UserService;
import com.flex.user_module.cache.RoleCacheService;
import com.flex.user_module.impl.entities.*;
import com.flex.user_module.impl.repositories.*;
import com.flex.user_module.impl.services.helpers.UserServiceHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.UDecoder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private final RoleCacheService roleCacheService;

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final UserRepository userRepository;
    private final UserLoginRepository userLoginRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final UserStatusRepository userStatusRepository;
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

        String providerId = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        //create service provider entity
        ServiceProvider serviceProvider = ServiceProvider.builder()
                .name(register.getProvider())
                .email(register.getProviderEmail())
                .providerId(providerId)
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

        //create permissions for admin
        List<Permission> permissions = permissionRepository.findAll();

        if (permissions.isEmpty()) {
            return CONFLICT("Permissions not found");
        }

        List<RolePermission> rolePermissions = permissions.stream().map(
                p -> RolePermission.builder().role(admin).permission(p).build()
        ).collect(Collectors.toList());

        //create user entity for admin
        User user = User.builder()
                .fName(register.getAdminFName())
                .lName(register.getAdminLName())
                .email(register.getAdminEmail())
                .role(admin)
                .serviceProvider(serviceProvider)
                .userType(ADMIN)
                .password(HashUtil.hash(register.getAdminPassword()))
                .build();

        UserDetails userDetails = UserDetails.builder()
                .nic(CryptoUtil.encrypt(register.getNic()))
                .user(user)
                .addedTime(new Date())
                .build();

        roleRepository.save(admin);
        rolePermissionRepository.saveAll(rolePermissions);
        userRepository.save(user);
        userDetailsRepository.save(userDetails);

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

        if (user.getUserType() != ADMIN && user.getRole() == null) {
            return CONFLICT("Invalid role");
        }

        //if has any previous login with no logout(logged in and close the browser without logout)
        // , logout from every login
        userServiceHelper.logoutFromPreviousLogins(user.getId());

        Map<String, Object> claims = new HashMap<>();

        claims.put("user", user.getId());
        claims.put("type", user.getUserType());
        claims.put("provider", user.getServiceProvider().getProviderId());
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

    @Override
    public ResponseEntity<?> logout(HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        User user = userRepository.findByIdAndDeletedIsFalse(userClaims.getUserId());

        if (user == null) {
            return CONFLICT("User not found");
        }

        userServiceHelper.logoutFromPreviousLogins(user.getId());
        roleCacheService.evictPermissionsCache(user.getId(), user.getRole().getRole());

        return SUCCESS("Successfully logout");
    }

    @Override
    public ResponseEntity<?> headerData(HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        User user = userRepository.findByIdAndDeletedIsFalse(userClaims.getUserId());

        if (user == null) {
            return CONFLICT("User not found");
        }

        return DATA(
                HeaderData.builder()
                        .userType(user.getUserType() == 0 ? "USER"
                                : user.getUserType() == 1 ? "ADMIN"
                                : "CUSTOMER")
                        .email(user.getEmail())
                        .serviceCenter(user.getRole().getServiceProvider().getName())
                        .userName(user.getFName())
                        .image(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<?> permissions(HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        User user = userRepository.findByIdAndDeletedIsFalse(userClaims.getUserId());

        if (user == null) {
            return CONFLICT("User not found");
        }

        if (user.getRole() == null || user.getRole().isDeleted()) {
            return CONFLICT("User has no role");
        }

        List<RolePermission> permissions = rolePermissionRepository
                .getAllRolePermissions(user.getRole().getId());

        if (permissions.isEmpty()) {
            return CONFLICT("No permissions for the role");
        }

        List<Integer> permissionIds = permissions.stream().map(
                RolePermission::getId
        ).collect(Collectors.toList());

        return DATA(permissionRepository.getPermissionsByIds(permissionIds));
    }

    @Override
    public ResponseEntity<?> employeeRegister(EmployeeRegister employeeRegister, HttpServletRequest request) {
        log.info(request.getRequestURI(), "{} body - {}", employeeRegister);

        String validationStatus = userServiceHelper
                .employeeRegisterValidation(employeeRegister);

        if (!validationStatus.equals("success")) {
            return CONFLICT(validationStatus);
        }

        UserDetails prevUserDetails = userDetailsRepository.findByNic(
                CryptoUtil.encrypt(employeeRegister.getNic())
        );

        if (prevUserDetails != null
                && !prevUserDetails.getUser().isDeleted()
                && prevUserDetails.getUser().getUserType() == USER) {
            return CONFLICT("Employee already exist by this NIC");
        }

        ServiceProvider serviceProvider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(employeeRegister.getProviderId());

        if (serviceProvider == null) {
            return CONFLICT("Invalid service provider");
        }

        User user = User.builder()
                .fName(employeeRegister.getFName())
                .lName(employeeRegister.getLName())
                .email(employeeRegister.getEmail())
                .build();

        UserDetails userDetails = UserDetails.builder()
                .nic(CryptoUtil.encrypt(employeeRegister.getNic()))
                .contact(CryptoUtil.encrypt(employeeRegister.getContact()))
                .addedTime(new Date())
                .user(user)
                .build();

        UserStatus userStatus = UserStatus.builder()
                .user(user)
                .providerApproved(false)
                .build();

        userRepository.save(user);
        userDetailsRepository.save(userDetails);
        userStatusRepository.save(userStatus);

        return SUCCESS("Registration Completed, Please wait to provider's confirmation.");
    }
}
