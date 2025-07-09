package com.shibam.learning.features.auth.dto.req;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
