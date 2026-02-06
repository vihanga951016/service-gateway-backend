package com.flex.service_module.api.http.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCluster {
    private Integer id;
    private String name;
    private List<Integer> serviceIds;
}
