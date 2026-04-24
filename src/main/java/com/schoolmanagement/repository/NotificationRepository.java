package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Notification;
import com.schoolmanagement.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTargetRoleOrderByCreatedAtDesc(Role targetRole);
    long countByTargetRoleAndReadFalse(Role targetRole);
}
