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
public class JobsSchedule {
    private Integer id;
    private Integer jobAtPointId;
    private Integer jobId;
    private String customerName;
    private String pointName;
    private String serviceName;
    private String status;
    private Integer totalTime;
    private String fromTo;
    private boolean freeSlot;
}
