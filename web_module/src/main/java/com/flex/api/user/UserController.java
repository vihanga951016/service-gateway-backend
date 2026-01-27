package com.flex.api.user;

// import com.flex.user_module.api.http.requests.Login;
import com.flex.user_module.api.http.requests.EmployeeRegister;
import com.flex.user_module.api.http.requests.Register;
import com.flex.user_module.api.http.requests.Login;
import com.flex.user_module.api.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/15/2026
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Register register, HttpServletRequest request) {
        return userService.register(register, request);
    }

    //todo create a service for customer registration - google OAuth

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login, HttpServletRequest request) {
        return userService.login(login, request);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return userService.logout(request);
    }

    @GetMapping("/header-data")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> headerData(HttpServletRequest request) {
        return userService.headerData(request);
    }

    @GetMapping("/load-permissions")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> loadPermissions(HttpServletRequest request) {
        return userService.permissions(request);
    }

    @PostMapping("/employee-register")
    public ResponseEntity<?> employeeRegister(@RequestBody EmployeeRegister employeeRegister, HttpServletRequest request) {
        return userService.employeeRegister(employeeRegister, request);
    }
}
