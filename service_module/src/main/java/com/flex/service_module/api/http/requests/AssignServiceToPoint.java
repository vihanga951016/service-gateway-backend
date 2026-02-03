package com.flex.service_module.api.http.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignServiceToPoint {
    private Integer serviceId;
    private Integer pointId;
}
