package com.flex.job_module.api.http.DTO;

import java.time.LocalTime;

public interface JobTimelineProjection {
    Integer getJobAtPointId();
    Integer getJobId();
    String getCustomerName();
    String getServiceName();
    String getStatus();
    LocalTime getStartTime();
    LocalTime getEndTime();
    Integer getPointId();
    String getFromTo();
}
