package com.flex.service_module.api.http.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoadCenterServices {

    private Integer serviceId;
    private String service;
    private boolean cluster;
    
}
