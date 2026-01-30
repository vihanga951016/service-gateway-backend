package com.flex.api.user;

// import com.flex.user_module.api.http.requests.Login;
import com.flex.common_module.http.pagination.Pagination;
import com.flex.user_module.api.http.requests.*;
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

    @PostMapping("/get-all")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).UM)")
    public ResponseEntity<?> getAll(@RequestBody Pagination pagination, HttpServletRequest request) {
        return userService.getAllUsers(pagination, request);
    }

    @PostMapping("/decrypt")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).DD)")
    public ResponseEntity<?> decrypt(@RequestBody DecryptValue value, HttpServletRequest request) {
        return userService.decryptString(value, request);
    }

    @PostMapping("/add")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).UM)")
    public ResponseEntity<?> addUser(@RequestBody AddUser addUser, HttpServletRequest request) {
        return userService.addUser(addUser, request);
    }

    @GetMapping("/{id}/get")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).UM)")
    public ResponseEntity<?> getUser(@PathVariable Integer id, HttpServletRequest request) {
        return userService.getUser(id, request);
    }

    @GetMapping("/profile-data")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> profileData(HttpServletRequest request) {
        return userService.userProfileData(request);
    }

    @GetMapping("/profile-details")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> profileDetails(HttpServletRequest request) {
        return userService.userProfileDetails(request);
    }

    @PostMapping("/update")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).UM)")
    public ResponseEntity<?> updateUser(@RequestBody AddUser addUser, HttpServletRequest request) {
        return userService.updateUser(addUser, request);
    }

    @PostMapping("/update-profile")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).UM)")
    public ResponseEntity<?> updateUserProfile(@RequestBody AddUser addUser, HttpServletRequest request) {
        return userService.updateUserProfile(addUser, request);
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).UM)")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id, HttpServletRequest request) {
        return userService.deleteUser(id, request);
    }

}
