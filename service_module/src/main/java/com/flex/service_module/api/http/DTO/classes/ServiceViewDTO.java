package com.flex.service_module.api.http.DTO.classes;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.Date;

@Data
@Builder
public class ServiceViewDTO {
    private Integer id;
    private Integer orderNumber;
    private String name;
    private String description;
    private LocalTime serviceTime;
    private boolean serviceTimeDepends;
    private String fServiceTime;
    private Integer totalPrice;
    private boolean totalPriceDepends;
    private Integer downPrice;
}
