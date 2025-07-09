package com.shibam.learning.features.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/v1/api/user/profile")
    public ResponseEntity<String> userProfile() {
        return ResponseEntity.ok("User profile information");
    }
}
