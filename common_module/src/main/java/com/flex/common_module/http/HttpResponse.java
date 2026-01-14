package com.flex.common_module.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpResponse<T> {
    private String message;
    private String timestamp = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().toString();
    private T data;

    public HttpResponse<T> responseOk(T data) {
        message = "success";
        this.data = data;
        return this;
    }

    public HttpResponse<T> responseFail(T data) {
        message = "error";
        this.data = data;
        return this;
    }

    public HttpResponse<T> serverError(T data) {
        message = "Something went wrong";
        this.data = data;
        return this;
    }
}
