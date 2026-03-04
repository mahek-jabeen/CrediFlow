package com.loanmanagement.controller;

import com.loanmanagement.entity.Notification;
import com.loanmanagement.entity.User;
import com.loanmanagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/user/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserNotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<?> getUserNotifications() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("Error: User not authenticated");
            }

            List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(currentUser);
            
            // Create safe response list
            List<Map<String, Object>> response = new java.util.ArrayList<>();
            if (notifications != null) {
                for (Notification notification : notifications) {
                    Map<String, Object> notifEntry = new HashMap<>();
                    notifEntry.put("id", notification.getId());
                    notifEntry.put("message", notification.getMessage());
                    notifEntry.put("isRead", notification.getIsRead());
                    notifEntry.put("createdAt", notification.getCreatedAt());
                    response.add(notifEntry);
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching user notifications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching notifications: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("Error: User not authenticated");
            }

            Notification notification = notificationRepository.findById(id).orElse(null);
            if (notification == null) {
                return ResponseEntity.badRequest().body("Error: Notification not found");
            }

            // Verify notification belongs to current user
            if (!notification.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().body("Error: Access denied");
            }

            notification.setIsRead(true);
            notificationRepository.save(notification);

            return ResponseEntity.ok("Notification marked as read");
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error marking notification as read: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            return null;
        }
    }
}
