package com.shibam.learning.features.auth.dto.req;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
