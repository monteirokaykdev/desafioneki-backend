package com.projetoneki.backend.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.projetoneki.backend.dto.Request.AdminRequest;
import com.projetoneki.backend.dto.Response.AdminResponse;
import com.projetoneki.backend.services.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<AdminResponse> create(
        @Valid @RequestBody AdminRequest request
    ) {
        AdminResponse createdAdmin =
            adminService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdAdmin);
    }

    @GetMapping("/me")
    public ResponseEntity<AdminResponse> getMe(
        Authentication authentication
    ) {
        AdminResponse admin =
            adminService.getMe(authentication);
        return ResponseEntity.ok(admin);
    }
}

