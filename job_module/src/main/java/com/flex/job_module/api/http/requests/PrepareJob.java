package com.flex.job_module.api.http.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrepareJob {
    private String customer;
    private String phone;
    private Integer centerClusterId;
    private List<Integer> servicesIds;
    private Integer serviceCenterId;
    private LocalDate appointmentDate;
    private String notes;
}
