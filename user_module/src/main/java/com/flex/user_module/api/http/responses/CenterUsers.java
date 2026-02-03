package com.flex.user_module.api.http.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CenterUsers {
    private Integer userId;
    private String userName;
    private String contact;
    private String email;
    private String role;
}
