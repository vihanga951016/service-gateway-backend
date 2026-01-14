package com.flex.user_module.impl.services;

import com.flex.common_module.http.ReturnResponse;
import com.flex.service_module.impl.repositories.ServiceProviderRepository;
import com.flex.user_module.api.http.requests.Register;
import com.flex.user_module.api.services.UserService;
import com.flex.user_module.impl.repositories.RoleRepository;
import com.flex.user_module.impl.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.flex.common_module.http.ReturnResponse.*;

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

    private final ServiceProviderRepository serviceProviderRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public ResponseEntity<?> register(Register register, HttpServletRequest request) {
        log.info(request.getRequestURI());

        if (serviceProviderRepository.existsByNameAndEmailAndDeletedIsFalse(
                register.getProvider(), register.getProviderEmail()
        )) {
            return CONFLICT("Service provider already registered");
        }

        if (userRepository.existsByEmailAndDeletedIsFalse(register.getAdminEmail())) {
            return CONFLICT("User already registered");
        }

        

        return null;
    }
}
