package com.loanmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    @Column(nullable = false)
    private Double requestedAmount;

    @Column(nullable = false)
    private Integer requestedTenureMonths;

    private Double proposedInterestRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private String remarks;

    private Integer creditScore;

    private Double existingEmiAmount;

    @Column(nullable = false)
    private LocalDateTime applicationDate;

    private LocalDateTime reviewedDate;

    private String reviewedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "loanApplication", cascade = CascadeType.ALL)
    private Loan loan;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        applicationDate = LocalDateTime.now();

        if (status == null) {
            status = ApplicationStatus.PENDING;
        }

        if (loanType == null) {
            loanType = LoanType.PERSONAL_LOAN;   // 🔥 FORCE DEFAULT HERE
        }

        if (applicationNumber == null) {
            applicationNumber = "APP-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum LoanType {
        HOME_LOAN,
        PERSONAL_LOAN,
        CAR_LOAN,
        EDUCATION_LOAN,
        BUSINESS_LOAN,
        GOLD_LOAN
    }

    public enum ApplicationStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        CANCELLED
    }
}
