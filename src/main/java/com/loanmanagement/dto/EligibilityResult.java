package com.loanmanagement.dto;

/**
 * Simple DTO class for loan eligibility result
 */
public class EligibilityResult {

    private boolean eligible;
    private String reason;

    /**
     * Default constructor
     */
    public EligibilityResult() {
    }

    /**
     * Constructor with all fields
     *
     * @param eligible whether the loan is eligible
     * @param reason   the reason for eligibility or rejection
     */
    public EligibilityResult(boolean eligible, String reason) {
        this.eligible = eligible;
        this.reason = reason;
    }

    /**
     * Get the eligibility status
     *
     * @return true if eligible, false otherwise
     */
    public boolean isEligible() {
        return eligible;
    }

    /**
     * Set the eligibility status
     *
     * @param eligible the eligibility status
     */
    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    /**
     * Get the reason for eligibility or rejection
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Set the reason for eligibility or rejection
     *
     * @param reason the reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
}
