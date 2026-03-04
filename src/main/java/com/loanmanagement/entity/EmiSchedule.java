package com.loanmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emi_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private Integer emiNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private Double emiAmount;

    @Column(nullable = false)
    private Double principalComponent;

    @Column(nullable = false)
    private Double interestComponent;

    @Column(nullable = false)
    private Double remainingPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private LocalDate paymentDate;

    private Double amountPaid;

    private Double lateFee;

    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        paymentStatus = PaymentStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        OVERDUE,
        PARTIAL_PAID,
        WAIVED
    }

    public enum PaymentMethod {
        CASH,
        CHEQUE,
        ONLINE_TRANSFER,
        UPI,
        DEBIT_CARD,
        CREDIT_CARD,
        AUTO_DEBIT
    }
}
