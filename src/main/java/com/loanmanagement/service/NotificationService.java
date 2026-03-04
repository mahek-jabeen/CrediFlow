package com.loanmanagement.service;

import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.entity.Notification;
import com.loanmanagement.entity.User;
import com.loanmanagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public void createNotification(User user, String message) {
        try {
            if (user == null || message == null || message.trim().isEmpty()) {
                log.warn("Cannot create notification: user or message is null/empty");
                return;
            }
            
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsRead(false);
            
            notificationRepository.save(notification);
            log.debug("Notification created for user {}: {}", user.getEmail(), message);
        } catch (Exception e) {
            log.error("Failed to create notification for user {}: {}", user != null ? user.getEmail() : "null", e.getMessage());
            // Don't throw - notification failure shouldn't break main flow
        }
    }
    
    public void createLoanStatusNotification(User user, LoanApplication loanApplication) {
        try {
            if (user == null || loanApplication == null) {
                return;
            }
            
            String message = String.format("Your loan application %s has been %s", 
                loanApplication.getApplicationNumber(), 
                loanApplication.getStatus());
            
            createNotification(user, message);
        } catch (Exception e) {
            log.error("Failed to create loan status notification: {}", e.getMessage());
        }
    }
}
