package com.flex.service_module.api.http.DTO;

public interface CenterClusterServicesData {
    Integer getId();

    String getService();

    Integer getTotal();

    Integer getDownPay();

    Integer getOrderNumber();

    String getServiceTime(); // HH:mm:ss

    Boolean getDisabled();
}
