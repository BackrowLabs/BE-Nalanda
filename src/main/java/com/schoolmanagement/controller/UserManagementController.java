package com.schoolmanagement.controller;

import com.schoolmanagement.dto.request.CreateUserRequest;
import com.schoolmanagement.dto.response.UserResponse;
import com.schoolmanagement.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    public List<UserResponse> listUsers() {
        return userManagementService.listUsers();
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userManagementService.createUser(request));
    }

    @PatchMapping("/{id}/role")
    public UserResponse updateRole(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return userManagementService.updateRole(id, body.get("role"));
    }

    @PatchMapping("/{id}/toggle-active")
    public UserResponse toggleActive(@PathVariable UUID id) {
        return userManagementService.toggleActive(id);
    }
}
