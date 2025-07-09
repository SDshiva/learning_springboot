package com.shibam.learning.features.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
    @GetMapping("/v1/api/admin/control")
    public ResponseEntity<String> adminControlPanel() {
        return ResponseEntity.ok("Admin control panel");
    }
}
