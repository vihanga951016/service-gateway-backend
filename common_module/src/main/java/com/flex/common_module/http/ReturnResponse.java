package com.flex.common_module.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
@SuppressWarnings("Duplicates")
@Slf4j
public class ReturnResponse {
    private static String getRequestURI() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            log.warn("Unable to get request URI: {}", e.getMessage());
        }
        return "Unknown URI";
    }

    public static ResponseEntity<?> BAD_REQUEST(String message) {
        log.warn("BAD_REQUEST for URI: {}", getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new HttpResponse<>().responseFail(message, 0));
    }

    public static ResponseEntity<?> CONFLICT(String message) {
        log.warn("CONFLICT for URI: {}", getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new HttpResponse<>().responseFail(message, 0));
    }

    public static ResponseEntity<?> SERVER_ERROR(String message) {
        log.warn("INTERNAL_SERVER_ERROR for URI: {}", getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new HttpResponse<>().serverError(message));
    }


    public static ResponseEntity<?> SUCCESS(String message) {
        log.info(getRequestURI(), "{} completed");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new HttpResponse<>().responseOk(message));
    }

    public static<T> ResponseEntity<?> DATA(T data) {
        log.info(getRequestURI(), "{} completed");
        return ResponseEntity.ok().body(new HttpResponse<>()
                .responseOk(data));
    }

    public static ResponseEntity<?> UNAUTHORIZED(String message) {
        log.warn("UNAUTHORIZED for URI: {}", getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new HttpResponse<>().responseFail(message, 0));
    }

    public static void FORBIDDEN(ObjectMapper objectMapper, HttpServletResponse response) throws IOException {
        if (!response.isCommitted()) {
            log.warn("FORBIDDEN for URI: {}", getRequestURI());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            HttpResponse<Object> body = new HttpResponse<>()
                    .responseFail("You are not allowed to access this resource", 1);

            String json = objectMapper.writeValueAsString(body);
            response.getWriter().write(json);
        }
    }
}
