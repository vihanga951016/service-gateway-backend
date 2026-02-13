package com.flex.job_module.api.http.responses;

import com.flex.job_module.impl.entities.JobAtPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreparedJob {
    private Integer jobId;
    private Integer customerId;
    private String appointmentDate;
    private String appointmentTime;
    private List<JobAtPoint> jobsAtPoint;
}
