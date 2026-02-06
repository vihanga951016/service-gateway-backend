package com.flex.service_module.api.http.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClusterData {

    private Integer id;
    private String name;
    private List<ClusterServiceData> services;
}
