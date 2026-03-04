package com.loanmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalRequest {
    private String applicationNumber;
    private String action; // APPROVE or REJECT
    private String rejectionReason; // Optional, only for REJECT
}
