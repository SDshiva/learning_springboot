package com.shibam.learning.features.auth.controller;

import com.shibam.learning.features.auth.dto.req.LoginRequest;
import com.shibam.learning.features.auth.dto.req.SignupRequest;
import com.shibam.learning.features.auth.dto.req.TokenRefreshRequest;
import com.shibam.learning.features.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest request) {
        return authService.register(request);
    }

    // ADMIN ONLY: create moderator
    @PostMapping("/create/moderator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createModerator(@RequestBody SignupRequest request) {
        return authService.createModerator(request);
    }

    // (Optional) ADMIN ONLY: create admin user
    @PostMapping("/create/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUserByAdmin(@RequestBody SignupRequest request) {
        return authService.createUserByAdmin(request);
    }

    // (Optional) MODERATOR ONLY: create admin user
    @PostMapping("/create/user")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<?> createUserByModerator(@RequestBody SignupRequest request) {
        return authService.createUserByModerator(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest request) {
        return authService.login(request);
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        return authService.refreshAccessToken(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestParam String username) {
        authService.logout(username);
        return ResponseEntity.ok("User logged out successfully");
    }
}
