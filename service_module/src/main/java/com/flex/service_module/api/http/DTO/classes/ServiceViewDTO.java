package com.flex.service_module.api.http.DTO.classes;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ServiceViewDTO {
    private Integer id;
    private String name;
    private String description;
    private Date serviceTime;
    private boolean serviceTimeDepends;
    private String fServiceTime;
    private Integer totalPrice;
    private boolean totalPriceDepends;
    private Integer downPrice;
}
