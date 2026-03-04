package com.loanmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for loan eligibility evaluation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityRequest {

    /**
     * User's monthly income
     */
    private Double monthlyIncome;

    /**
     * User's monthly expenses
     */
    private Double monthlyExpenses;

    /**
     * User's credit score
     */
    private Integer creditScore;

    /**
     * Requested loan amount
     */
    private Double requestedLoanAmount;

    /**
     * Loan tenure in months
     */
    private Integer tenureMonths;

    /**
     * Annual interest rate (in percentage)
     */
    private Double annualInterestRate;

    // Additional fields for AdminService compatibility
    private Double existingEmiAmount;
    private Double requestedAmount;
    private Integer tenure;
    private Double interestRate;
}
