package com.flex.user_module.api.services;

import com.flex.common_module.http.pagination.Pagination;
import com.flex.user_module.api.http.requests.*;
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

    ResponseEntity<?> headerData(HttpServletRequest request);

    ResponseEntity<?> permissions(HttpServletRequest request);

    ResponseEntity<?> employeeRegister(EmployeeRegister employeeRegister, HttpServletRequest request);

    ResponseEntity<?> getAllUsers(Pagination pagination, HttpServletRequest request);

    ResponseEntity<?> decryptString(DecryptValue value, HttpServletRequest request);

    ResponseEntity<?> addUser(AddUser addUser, HttpServletRequest request);

    ResponseEntity<?> getUser(Integer userId, HttpServletRequest request);

    ResponseEntity<?> updateUser(AddUser addUser, HttpServletRequest request);

    ResponseEntity<?> deleteUser(Integer id, HttpServletRequest request);

    //todo create a service for customer registration - google OAuth
}
