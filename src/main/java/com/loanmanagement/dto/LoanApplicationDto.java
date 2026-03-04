package com.loanmanagement.dto;

import com.loanmanagement.entity.LoanApplication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationDto {
    private Long id;
    private String applicationNumber;
    private String userName;
    private String userEmail;
    private Long userId;
    private LoanApplication.LoanType loanType;
    private Double requestedAmount;
    private Integer requestedTenureMonths;
    private Double proposedInterestRate;
    private LoanApplication.ApplicationStatus status;
    private String remarks;
    private Integer creditScore;
    private Double existingEmiAmount;
    private LocalDateTime applicationDate;
    private LocalDateTime reviewedDate;
    private String reviewedBy;
    
    // Additional fields for admin view
    private Boolean eligible;
    private String rejectionReason;
}
