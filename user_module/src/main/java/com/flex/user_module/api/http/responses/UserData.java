package com.flex.user_module.api.http.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
    private Integer userId;
    private String fName;
    private String lName;
    private String email;
    private Integer userType;
    private Integer roleId;
    private Integer serviceCenterId;
    private String nic;
    private String contact;
}
