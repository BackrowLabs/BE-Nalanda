package com.schoolmanagement.service;

import com.schoolmanagement.dto.response.NotificationResponse;
import com.schoolmanagement.entity.Notification;
import com.schoolmanagement.enums.Role;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getForRole(Role role) {
        return notificationRepository.findByTargetRoleOrderByCreatedAtDesc(role)
                .stream().map(this::toResponse).toList();
    }

    public long getUnreadCount(Role role) {
        return notificationRepository.countByTargetRoleAndReadFalse(role);
    }

    @Transactional
    public void markRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        n.setRead(true);
    }

    @Transactional
    public void create(String message, Role targetRole, Long relatedEntityId, String createdByName) {
        notificationRepository.save(Notification.builder()
                .message(message)
                .targetRole(targetRole)
                .relatedEntityId(relatedEntityId)
                .createdByName(createdByName)
                .build());
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getMessage(),
                n.getRelatedEntityId(),
                n.isRead(),
                n.getCreatedByName(),
                n.getCreatedAt()
        );
    }
}
