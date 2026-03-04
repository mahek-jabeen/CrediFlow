package com.loanmanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoanListResponse {
    
    private Long applicationId;
    private String applicationNumber;
    private String userName;
    private String userEmail;
    private String loanType;
    private Double requestedAmount;
    private Integer tenure;
    private String status;
    private LocalDateTime applicationDate;
    private LocalDateTime reviewedDate;
}
