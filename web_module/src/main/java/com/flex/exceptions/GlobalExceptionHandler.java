package com.flex.exceptions;

import com.flex.common_module.constants.Colors;
import com.flex.common_module.exceptions.UserTokenException;
import com.flex.user_module.exceptions.MissingRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

import static com.flex.common_module.http.ReturnResponse.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error(Colors.YELLOW + "{}" + Colors.RESET, Objects.requireNonNull(ex.getRootCause()).getMessage());
        ex.printStackTrace();
        return SERVER_ERROR("Data conflict");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AuthorizationDeniedException ex) {
        log.error(Colors.YELLOW + ex.getMessage() + Colors.RESET);
        ex.printStackTrace();
        return UNAUTHORIZED("No permission for do this");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        log.error(Colors.YELLOW + ex.getMessage() + Colors.RESET);
        ex.printStackTrace();
        return SERVER_ERROR("An unexpected error occurred");
    }

    @ExceptionHandler(MissingRoleException.class)
    public ResponseEntity<?> handleMissingDesignation(MissingRoleException ex) {
        log.error(Colors.YELLOW + ex.getMessage() + Colors.RESET);
        ex.printStackTrace();
        return CONFLICT(ex.getMessage());
    }

    @ExceptionHandler(UserTokenException.class)
    public ResponseEntity<?> userTokenException(UserTokenException ex) {
        log.error(Colors.YELLOW + "{}" + Colors.RESET, ex.getMessage());
        ex.printStackTrace();
        return UNAUTHORIZED(ex.getMessage());
    }
}
