package com.flex.user_module.api.services;

import com.flex.user_module.api.http.requests.EmployeeRegister;
import com.flex.user_module.api.http.requests.Login;
import com.flex.user_module.api.http.requests.Register;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
public interface UserService {

    ResponseEntity<?> register(Register register, HttpServletRequest request);

    ResponseEntity<?> login(Login login, HttpServletRequest request);

    ResponseEntity<?> logout(HttpServletRequest request);

    ResponseEntity<?> headerData(Integer userId, HttpServletRequest request);

    ResponseEntity<?> permissions(Integer userId, HttpServletRequest request);

    ResponseEntity<?> employeeRegister(EmployeeRegister employeeRegister, HttpServletRequest request);

    //todo create a service for customer registration - google OAuth
}
