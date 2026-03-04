package com.flex.service_module.api.http.DTO;

import java.time.LocalTime;

public interface CenterClusterServicesData {
    Integer getId();

    String getService();

    Integer getTotal();

    Integer getDownPay();

    Integer getOrderNumber();

    LocalTime getServiceTime(); // HH:mm:ss

    Boolean getDisabled();
}
