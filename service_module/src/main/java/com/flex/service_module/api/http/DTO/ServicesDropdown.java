package com.flex.service_module.api.http.DTO;

import java.time.LocalTime;

public interface ServicesDropdown {
    Integer getId();
    String getName();
    LocalTime getTime();
    Integer getTotalPrice();
    Integer getDownPrice();
}
