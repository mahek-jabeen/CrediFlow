package com.loanmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for loan eligibility evaluation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityResponse {

    /**
     * Indicates whether the loan application is eligible
     */
    private boolean eligible;

    /**
     * List of rejection reasons (empty if eligible)
     */
    @Builder.Default
    private List<String> rejectionReasons = new ArrayList<>();

    /**
     * Calculated monthly EMI amount
     */
    private Double calculatedEmi;

    /**
     * EMI to income ratio as a percentage
     */
    private Double emiToIncomeRatio;

    /**
     * Maximum eligible loan amount based on income
     */
    private Double maxEligibleLoanAmount;

    /**
     * Additional details or recommendations
     */
    private String message;

    /**
     * Add a rejection reason
     */
    public void addRejectionReason(String reason) {
        if (this.rejectionReasons == null) {
            this.rejectionReasons = new ArrayList<>();
        }
        this.rejectionReasons.add(reason);
    }
}
