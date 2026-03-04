package com.loanmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(nullable = false)
    private Double principalAmount;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false)
    private Double emiAmount;

    @Column(nullable = false)
    private Double totalPayableAmount;

    @Column(nullable = false)
    private Double totalInterest;

    @Column(nullable = false)
    private Double outstandingAmount;

    @Column(nullable = false)
    private LocalDate disbursementDate;

    @Column(nullable = false)
    private LocalDate firstEmiDate;

    private LocalDate lastEmiDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private Integer paidEmis;

    private Integer pendingEmis;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<EmiSchedule> emiSchedules;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        paidEmis = 0;
        pendingEmis = tenureMonths;
        outstandingAmount = totalPayableAmount;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum LoanStatus {
        ACTIVE,
        CLOSED,
        DEFAULTED,
        FORECLOSED
    }
}
