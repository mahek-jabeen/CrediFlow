package com.loanmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which loan this audit belongs to
    @Column(nullable = false)
    private Long loanApplicationId;

    // What happened
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    private LoanApplication.ApplicationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private LoanApplication.ApplicationStatus newStatus;

    // Who did it
    @Column(nullable = false)
    private String adminEmail;

    // Optional admin comment
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public enum AuditAction {
        CREATED,
        STATUS_CHANGED,
        APPROVED_BY_ADMIN,
        REJECTED_BY_ADMIN,
        MARKED_UNDER_REVIEW
    }
}
