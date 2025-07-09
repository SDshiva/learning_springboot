package com.shibam.learning.features.moderator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModeratorController {
    @GetMapping("v1/api/mod/dashboard")
    public ResponseEntity<String> moderatorDashboard() {
        return ResponseEntity.ok("Moderator dashboard information");
    }
}
