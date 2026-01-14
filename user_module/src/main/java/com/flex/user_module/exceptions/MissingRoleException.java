package com.flex.user_module.exceptions;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
public class MissingRoleException extends RuntimeException {
    public MissingRoleException(String message) {
        super(message);
    }
}
