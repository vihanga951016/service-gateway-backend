package com.flex.job_module.api.http.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubJobDetails {
    private String service;
    private String pointName;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime actualEndTime;
    private Integer status;
    private boolean completed;
    private boolean estimatedEndTime;
}
