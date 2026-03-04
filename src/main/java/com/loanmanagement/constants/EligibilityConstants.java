package com.loanmanagement.constants;

/**
 * Constants for loan eligibility evaluation rules
 */
public final class EligibilityConstants {

    private EligibilityConstants() {
        // Prevent instantiation
    }

    /**
     * Minimum credit score required for loan eligibility
     */
    public static final int MINIMUM_CREDIT_SCORE = 650;

    /**
     * Minimum valid credit score value (input validation)
     */
    public static final int MIN_VALID_CREDIT_SCORE = 300;

    /**
     * Maximum valid credit score value (input validation)
     */
    public static final int MAX_VALID_CREDIT_SCORE = 900;

    /**
     * Maximum loan amount multiplier based on monthly income
     * Loan amount should not exceed this multiplier times the monthly income
     */
    public static final int LOAN_AMOUNT_TO_INCOME_MULTIPLIER = 20;

    /**
     * Maximum EMI to income ratio (as a percentage)
     * EMI should not exceed this percentage of monthly income
     */
    public static final double MAX_EMI_TO_INCOME_RATIO = 40.0;

    /**
     * Maximum loan tenure in months
     */
    public static final int MAX_LOAN_TENURE_MONTHS = 60;

    /**
     * Number of months in a year (for interest rate calculations)
     */
    public static final int MONTHS_IN_YEAR = 12;

    /**
     * Percentage conversion factor
     */
    public static final double PERCENTAGE_FACTOR = 100.0;
}
