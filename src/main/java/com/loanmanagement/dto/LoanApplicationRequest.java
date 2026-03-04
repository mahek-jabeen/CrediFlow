package com.loanmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {
    
    private Double amount;
    private Integer tenure;
    private Double interestRate;
}
