package com.schoolmanagement.controller;

import com.schoolmanagement.dto.response.NotificationResponse;
import com.schoolmanagement.enums.Role;
import com.schoolmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<NotificationResponse> getAll() {
        return notificationService.getForRole(Role.ADMIN);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> getUnreadCount() {
        return Map.of("count", notificationService.getUnreadCount(Role.ADMIN));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.noContent().build();
    }
}
