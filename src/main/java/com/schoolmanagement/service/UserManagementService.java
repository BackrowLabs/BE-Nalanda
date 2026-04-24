package com.schoolmanagement.service;

import com.schoolmanagement.dto.request.CreateUserRequest;
import com.schoolmanagement.dto.response.UserResponse;
import com.schoolmanagement.entity.Profile;
import com.schoolmanagement.enums.Role;
import com.schoolmanagement.exception.BusinessException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserManagementService {

    private final ProfileRepository profileRepository;
    private final RestClient supabaseAdmin;

    public UserManagementService(
            ProfileRepository profileRepository,
            @Value("${app.supabase.url}") String supabaseUrl,
            @Value("${app.supabase.service-role-key}") String serviceRoleKey) {
        this.profileRepository = profileRepository;
        this.supabaseAdmin = RestClient.builder()
                .baseUrl(supabaseUrl + "/auth/v1/admin")
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();
    }

    public List<UserResponse> listUsers() {
        return profileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        Role role;
        try {
            role = Role.valueOf(req.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid role: " + req.role());
        }

        // Create auth user via Supabase Admin API
        Map<?, ?> supabaseUser;
        try {
            supabaseUser = supabaseAdmin.post()
                    .uri("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "email", req.email(),
                            "password", req.password(),
                            "email_confirm", true
                    ))
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Failed to create Supabase auth user: {}", e.getMessage());
            throw new BusinessException("Failed to create user in auth system: " + e.getMessage());
        }

        if (supabaseUser == null || supabaseUser.get("id") == null) {
            throw new BusinessException("Auth system returned no user ID");
        }

        UUID userId = UUID.fromString(supabaseUser.get("id").toString());

        Profile profile = Profile.builder()
                .id(userId)
                .email(req.email())
                .fullName(req.fullName())
                .role(role)
                .phone(req.phone())
                .active(true)
                .build();

        return toResponse(profileRepository.save(profile));
    }

    @Transactional
    public UserResponse updateRole(UUID userId, String roleName) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        try {
            profile.setRole(Role.valueOf(roleName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid role: " + roleName);
        }
        return toResponse(profileRepository.save(profile));
    }

    @Transactional
    public UserResponse toggleActive(UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        profile.setActive(!profile.isActive());
        return toResponse(profileRepository.save(profile));
    }

    private UserResponse toResponse(Profile p) {
        return new UserResponse(p.getId(), p.getEmail(), p.getFullName(),
                p.getPhone(), p.getRole(), p.isActive(), p.getCreatedAt());
    }
}
