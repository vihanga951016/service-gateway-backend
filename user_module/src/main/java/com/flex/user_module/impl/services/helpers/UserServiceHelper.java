package com.flex.user_module.impl.services.helpers;

import com.flex.common_module.security.impls.entities.ExpiredToken;
import com.flex.common_module.security.impls.repositories.ExpiredTokenRepository;
import com.flex.user_module.api.http.requests.EmployeeRegister;
import com.flex.user_module.impl.entities.UserLogin;
import com.flex.user_module.impl.repositories.UserLoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/15/2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceHelper {

    private final UserLoginRepository userLoginRepository;
    private final ExpiredTokenRepository expiredTokenRepository;

    //close all non-logout login.
    public void logoutFromPreviousLogins(Integer userId) {

        List<UserLogin> login = userLoginRepository.getAllLogin(userId);

        if (login.size() > 0) {
            log.info("user {}", userId , "{} has previous login");

            List<UserLogin> prevLoginFixes = login.stream().peek(
                    l -> {
                        l.setLogoutTime(new Date());
                        l.setLogout(true);
                        //this is doing if prevent to access for logout tokens
                        expiredTokenRepository.save(
                                ExpiredToken.builder()
                                        .id(l.getToken())
                                        .userId(userId)
                                        .build()
                        );
                    }
            ).collect(Collectors.toList());

            userLoginRepository.saveAll(prevLoginFixes);
        }
    }

    public String employeeRegisterValidation(EmployeeRegister e) {
        if (e.getFName() == null || e.getFName().isEmpty()) {
            return "First name should not empty";
        }

        if (e.getLName() == null || e.getLName().isEmpty()) {
            return "Last name should not empty";
        }

        if (e.getContact() == null || e.getContact().isEmpty()) {
            return "Contact number should not empty";
        }

        if (e.getNic() == null || e.getNic().isEmpty()) {
            return "NIC should not empty";
        }

        if (e.getProviderId() == null || e.getProviderId().isEmpty()) {
            return "Provider ID should not empty";
        }

        return "success";
    }
}
