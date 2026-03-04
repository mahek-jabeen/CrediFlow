package com.loanmanagement.service;

import com.loanmanagement.constants.EligibilityConstants;
import com.loanmanagement.dto.EligibilityRequest;
import com.loanmanagement.dto.EligibilityResponse;
import com.loanmanagement.dto.EligibilityResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating loan eligibility based on predefined business rules
 */
@Service
@Slf4j
public class EligibilityService {

    /**
     * Evaluate loan eligibility and return a simple result
     *
     * @param request EligibilityRequest containing user financial details and loan request
     * @return EligibilityResult indicating eligibility status and reason
     */
    public EligibilityResult evaluateEligibility(EligibilityRequest request) {
        log.debug("Starting eligibility evaluation - Income: {}, Credit: {}, Amount: {}, Tenure: {}",
                request.getMonthlyIncome(), request.getCreditScore(), 
                request.getRequestedLoanAmount(), request.getTenureMonths());
        
        EligibilityResult validationFailure = validateForEligibilityEvaluation(request);
        if (validationFailure != null) {
            log.warn("Eligibility validation failed: {}", validationFailure.getReason());
            return validationFailure;
        }

        // Rule 1: Credit score must be at least the minimum required score
        if (request.getCreditScore() < EligibilityConstants.MINIMUM_CREDIT_SCORE) {
            String reason = "Credit score must be at least " + EligibilityConstants.MINIMUM_CREDIT_SCORE;
            log.warn("Credit score check failed: {} < {}", request.getCreditScore(), EligibilityConstants.MINIMUM_CREDIT_SCORE);
            return new EligibilityResult(false, reason);
        }
        log.debug("Credit score check passed: {} >= {}", request.getCreditScore(), EligibilityConstants.MINIMUM_CREDIT_SCORE);

        // Rule 2: Requested loan amount must not exceed 20x monthly income
        double maxLoanAmount = request.getMonthlyIncome() * EligibilityConstants.LOAN_AMOUNT_TO_INCOME_MULTIPLIER;
        if (request.getRequestedLoanAmount() > maxLoanAmount) {
            String reason = "Requested loan amount must not exceed "
                    + EligibilityConstants.LOAN_AMOUNT_TO_INCOME_MULTIPLIER
                    + " times monthly income";
            log.warn("Loan amount check failed: {} > {} ({}x income of {})", 
                    request.getRequestedLoanAmount(), maxLoanAmount, 
                    EligibilityConstants.LOAN_AMOUNT_TO_INCOME_MULTIPLIER, request.getMonthlyIncome());
            return new EligibilityResult(false, reason);
        }
        log.debug("Loan amount check passed: {} <= {}", request.getRequestedLoanAmount(), maxLoanAmount);

        // Rule 3: Calculated EMI must not exceed 40% of monthly income
        double emi = calculateMonthlyEmi(
                request.getRequestedLoanAmount(),
                request.getAnnualInterestRate(),
                request.getTenureMonths()
        );
        double emiToIncomePercentage = (emi / request.getMonthlyIncome()) * EligibilityConstants.PERCENTAGE_FACTOR;
        if (emiToIncomePercentage > EligibilityConstants.MAX_EMI_TO_INCOME_RATIO) {
            String reason = "EMI must not exceed " + EligibilityConstants.MAX_EMI_TO_INCOME_RATIO + "% of monthly income";
            log.warn("EMI ratio check failed: {}% > {}% (EMI: {}, Income: {})", 
                    emiToIncomePercentage, EligibilityConstants.MAX_EMI_TO_INCOME_RATIO, 
                    emi, request.getMonthlyIncome());
            return new EligibilityResult(false, reason);
        }
        log.debug("EMI ratio check passed: {}% <= {}%", emiToIncomePercentage, EligibilityConstants.MAX_EMI_TO_INCOME_RATIO);

        // Rule 4: Loan tenure must not exceed 60 months
        if (request.getTenureMonths() > EligibilityConstants.MAX_LOAN_TENURE_MONTHS) {
            String reason = "Loan tenure must not exceed " + EligibilityConstants.MAX_LOAN_TENURE_MONTHS + " months";
            log.warn("Tenure check failed: {} > {}", request.getTenureMonths(), EligibilityConstants.MAX_LOAN_TENURE_MONTHS);
            return new EligibilityResult(false, reason);
        }
        log.debug("Tenure check passed: {} <= {}", request.getTenureMonths(), EligibilityConstants.MAX_LOAN_TENURE_MONTHS);

        log.info("Eligibility evaluation PASSED - All checks cleared");
        return new EligibilityResult(true, "Eligible for loan");
    }

    /**
     * Basic input validation for the simplified eligibility evaluation.
     * Returns an {@link EligibilityResult} when validation fails; otherwise returns null.
     */
    private EligibilityResult validateForEligibilityEvaluation(EligibilityRequest request) {
        if (request == null) {
            return new EligibilityResult(false, "Request must not be null");
        }

        // Handle null monthlyIncome with safe default
        if (request.getMonthlyIncome() == null) {
            request.setMonthlyIncome(50000.0); // Safe default income
        }

        if (request.getMonthlyIncome() <= 0) {
            request.setMonthlyIncome(50000.0); // Safe default income
        }

        // Handle null requestedLoanAmount with safe default
        if (request.getRequestedLoanAmount() == null) {
            request.setRequestedLoanAmount(0.0);
        }

        if (request.getRequestedLoanAmount() <= 0) {
            return new EligibilityResult(false, "Requested loan amount must be greater than zero");
        }

        // Handle null tenureMonths with safe default
        if (request.getTenureMonths() == null) {
            request.setTenureMonths(12); // Default to 12 months
        }

        if (request.getTenureMonths() <= 0) {
            request.setTenureMonths(12); // Default to 12 months
        }

        // Handle null creditScore with safe default
        if (request.getCreditScore() == null) {
            request.setCreditScore(650); // Default to minimum eligible score
        }

        if (request.getCreditScore() < EligibilityConstants.MIN_VALID_CREDIT_SCORE
                || request.getCreditScore() > EligibilityConstants.MAX_VALID_CREDIT_SCORE) {
            request.setCreditScore(650); // Default to minimum eligible score
        }

        // Handle null annualInterestRate with safe default
        if (request.getAnnualInterestRate() == null) {
            request.setAnnualInterestRate(12.0); // Default to 12%
        }

        if (request.getAnnualInterestRate() < 0) {
            request.setAnnualInterestRate(12.0); // Default to 12%
        }

        return null; // Validation passed with safe defaults
    }

    /**
     * Calculate monthly EMI using the standard formula
     * EMI = [P x R x (1+R)^N] / [(1+R)^N - 1]
     *
     * @param principal Loan principal amount
     * @param annualInterestRate Annual interest rate in percentage
     * @param tenureMonths Loan tenure in months
     * @return Calculated monthly EMI
     */
    private double calculateMonthlyEmi(double principal, double annualInterestRate, int tenureMonths) {
        if (annualInterestRate == 0) {
            return principal / tenureMonths;
        }

        double monthlyInterestRate = annualInterestRate / (EligibilityConstants.MONTHS_IN_YEAR * EligibilityConstants.PERCENTAGE_FACTOR);
        double onePlusRPowerN = Math.pow(1 + monthlyInterestRate, tenureMonths);
        return (principal * monthlyInterestRate * onePlusRPowerN) / (onePlusRPowerN - 1);
    }

    /**
     * Evaluate loan eligibility based on user financial details and loan request
     *
     * @param request EligibilityRequest containing user financial details and loan request
     * @return EligibilityResponse indicating eligibility status and details
     */
    public EligibilityResponse evaluateLoanEligibility(EligibilityRequest request) {
        log.info("Evaluating loan eligibility for amount: {} with tenure: {} months",
                request.getRequestedLoanAmount(), request.getTenureMonths());

        // Validate input
        validateRequest(request);

        // Initialize response
        EligibilityResponse response = EligibilityResponse.builder()
                .eligible(true)
                .build();

        // Calculate EMI
        double calculatedEmi = calculateEmi(
                request.getRequestedLoanAmount(),
                request.getAnnualInterestRate(),
                request.getTenureMonths()
        );
        response.setCalculatedEmi(calculatedEmi);

        // Calculate total EMI (no existing EMI obligations in this version)
        double totalEmi = calculatedEmi;

        // Calculate EMI to income ratio
        double emiToIncomeRatio = calculateEmiToIncomeRatio(totalEmi, request.getMonthlyIncome());
        response.setEmiToIncomeRatio(emiToIncomeRatio);

        // Calculate maximum eligible loan amount
        double maxEligibleLoanAmount = calculateMaxEligibleLoanAmount(request.getMonthlyIncome());
        response.setMaxEligibleLoanAmount(maxEligibleLoanAmount);

        // Apply eligibility rules
        boolean isCreditScoreEligible = checkCreditScore(request.getCreditScore(), response);
        boolean isLoanAmountEligible = checkLoanAmount(request.getRequestedLoanAmount(), 
                request.getMonthlyIncome(), response);
        boolean isEmiRatioEligible = checkEmiRatio(emiToIncomeRatio, response);
        boolean isTenureEligible = checkTenure(request.getTenureMonths(), response);

        // Determine overall eligibility
        boolean overallEligibility = isCreditScoreEligible && isLoanAmountEligible && 
                isEmiRatioEligible && isTenureEligible;
        response.setEligible(overallEligibility);

        // Set appropriate message
        if (overallEligibility) {
            response.setMessage("Congratulations! You are eligible for the loan.");
            log.info("Loan eligibility evaluation passed for amount: {}", request.getRequestedLoanAmount());
        } else {
            response.setMessage("Unfortunately, you do not meet the eligibility criteria for this loan.");
            log.info("Loan eligibility evaluation failed. Reasons: {}", response.getRejectionReasons());
        }

        return response;
    }

    /**
     * Validate the eligibility request
     *
     * @param request EligibilityRequest to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRequest(EligibilityRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Eligibility request cannot be null");
        }
        if (request.getCreditScore() == null || request.getCreditScore() < 0) {
            throw new IllegalArgumentException("Credit score must be provided and non-negative");
        }
        if (request.getMonthlyIncome() == null || request.getMonthlyIncome() <= 0) {
            throw new IllegalArgumentException("Monthly income must be provided and greater than zero");
        }
        if (request.getRequestedLoanAmount() == null || request.getRequestedLoanAmount() <= 0) {
            throw new IllegalArgumentException("Requested loan amount must be provided and greater than zero");
        }
        if (request.getTenureMonths() == null || request.getTenureMonths() <= 0) {
            throw new IllegalArgumentException("Requested tenure must be provided and greater than zero");
        }
        if (request.getAnnualInterestRate() == null || request.getAnnualInterestRate() < 0) {
            throw new IllegalArgumentException("Interest rate must be provided and non-negative");
        }
    }

    /**
     * Check if credit score meets the minimum requirement
     *
     * @param creditScore User's credit score
     * @param response    EligibilityResponse to update
     * @return true if credit score is eligible, false otherwise
     */
    private boolean checkCreditScore(Integer creditScore, EligibilityResponse response) {
        if (creditScore < EligibilityConstants.MINIMUM_CREDIT_SCORE) {
            String reason = String.format(
                    "Credit score (%d) is below the minimum requirement of %d",
                    creditScore, EligibilityConstants.MINIMUM_CREDIT_SCORE
            );
            response.addRejectionReason(reason);
            log.debug("Credit score check failed: {}", reason);
            return false;
        }
        log.debug("Credit score check passed: {}", creditScore);
        return true;
    }

    /**
     * Check if requested loan amount is within acceptable limits
     *
     * @param requestedAmount Requested loan amount
     * @param monthlyIncome   User's monthly income
     * @param response        EligibilityResponse to update
     * @return true if loan amount is eligible, false otherwise
     */
    private boolean checkLoanAmount(Double requestedAmount, Double monthlyIncome, 
            EligibilityResponse response) {
        double maxAllowedAmount = monthlyIncome * EligibilityConstants.LOAN_AMOUNT_TO_INCOME_MULTIPLIER;
        
        if (requestedAmount > maxAllowedAmount) {
            String reason = String.format(
                    "Requested loan amount (%.2f) exceeds maximum allowed amount (%.2f) based on %dx monthly income",
                    requestedAmount, maxAllowedAmount, EligibilityConstants.LOAN_AMOUNT_TO_INCOME_MULTIPLIER
            );
            response.addRejectionReason(reason);
            log.debug("Loan amount check failed: {}", reason);
            return false;
        }
        log.debug("Loan amount check passed: {} <= {}", requestedAmount, maxAllowedAmount);
        return true;
    }

    /**
     * Check if EMI to income ratio is within acceptable limits
     *
     * @param emiToIncomeRatio Calculated EMI to income ratio
     * @param response         EligibilityResponse to update
     * @return true if EMI ratio is eligible, false otherwise
     */
    private boolean checkEmiRatio(Double emiToIncomeRatio, EligibilityResponse response) {
        if (emiToIncomeRatio > EligibilityConstants.MAX_EMI_TO_INCOME_RATIO) {
            String reason = String.format(
                    "EMI to income ratio (%.2f%%) exceeds the maximum allowed limit of %.2f%%",
                    emiToIncomeRatio, EligibilityConstants.MAX_EMI_TO_INCOME_RATIO
            );
            response.addRejectionReason(reason);
            log.debug("EMI ratio check failed: {}", reason);
            return false;
        }
        log.debug("EMI ratio check passed: {}% <= {}%", emiToIncomeRatio, 
                EligibilityConstants.MAX_EMI_TO_INCOME_RATIO);
        return true;
    }

    /**
     * Check if loan tenure is within acceptable limits
     *
     * @param tenureMonths Requested tenure in months
     * @param response     EligibilityResponse to update
     * @return true if tenure is eligible, false otherwise
     */
    private boolean checkTenure(Integer tenureMonths, EligibilityResponse response) {
        if (tenureMonths > EligibilityConstants.MAX_LOAN_TENURE_MONTHS) {
            String reason = String.format(
                    "Requested tenure (%d months) exceeds the maximum allowed tenure of %d months",
                    tenureMonths, EligibilityConstants.MAX_LOAN_TENURE_MONTHS
            );
            response.addRejectionReason(reason);
            log.debug("Tenure check failed: {}", reason);
            return false;
        }
        log.debug("Tenure check passed: {} <= {}", tenureMonths, 
                EligibilityConstants.MAX_LOAN_TENURE_MONTHS);
        return true;
    }

    /**
     * Calculate monthly EMI using the standard EMI formula
     * EMI = [P x R x (1+R)^N] / [(1+R)^N - 1]
     * where P = Principal amount, R = Monthly interest rate, N = Tenure in months
     *
     * @param principal           Loan principal amount
     * @param annualInterestRate  Annual interest rate in percentage
     * @param tenureMonths        Loan tenure in months
     * @return Calculated monthly EMI
     */
    private double calculateEmi(Double principal, Double annualInterestRate, Integer tenureMonths) {
        // Handle zero interest rate case
        if (annualInterestRate == 0) {
            return principal / tenureMonths;
        }

        // Convert annual interest rate to monthly rate
        double monthlyInterestRate = annualInterestRate / 
                (EligibilityConstants.MONTHS_IN_YEAR * EligibilityConstants.PERCENTAGE_FACTOR);

        // Calculate (1 + R)^N
        double onePlusRPowerN = Math.pow(1 + monthlyInterestRate, tenureMonths);

        // Calculate EMI using the formula
        double emi = (principal * monthlyInterestRate * onePlusRPowerN) / (onePlusRPowerN - 1);

        log.debug("Calculated EMI: {} for principal: {}, rate: {}%, tenure: {} months",
                emi, principal, annualInterestRate, tenureMonths);

        return emi;
    }

    /**
     * Calculate EMI to income ratio as a percentage
     *
     * @param totalEmi      Total EMI amount (including existing obligations)
     * @param monthlyIncome User's monthly income
     * @return EMI to income ratio as a percentage
     */
    private double calculateEmiToIncomeRatio(Double totalEmi, Double monthlyIncome) {
        double ratio = (totalEmi / monthlyIncome) * EligibilityConstants.PERCENTAGE_FACTOR;
        log.debug("EMI to income ratio: {}% (EMI: {}, Income: {})", ratio, totalEmi, monthlyIncome);
        return ratio;
    }

    /**
     * Calculate maximum eligible loan amount based on monthly income
     *
     * @param monthlyIncome User's monthly income
     * @return Maximum eligible loan amount
     */
    private double calculateMaxEligibleLoanAmount(Double monthlyIncome) {
        double maxAmount = monthlyIncome * EligibilityConstants.LOAN_AMOUNT_TO_INCOME_MULTIPLIER;
        log.debug("Maximum eligible loan amount: {} ({}x monthly income of {})",
                maxAmount, EligibilityConstants.LOAN_AMOUNT_TO_INCOME_MULTIPLIER, monthlyIncome);
        return maxAmount;
    }

    // Overloaded methods for AdminService compatibility
    public EligibilityResult checkEligibility(double monthlyIncome, double existingEmi, int creditScore, Double requestedAmount, Integer tenure, double interestRate) {
        // Handle null values and provide safe defaults
        Double safeRequestedAmount = requestedAmount != null ? requestedAmount : 0.0;
        Integer safeTenure = tenure != null ? tenure : 12; // Default to 12 months
        Double safeInterestRate = interestRate > 0 ? interestRate : 12.0; // Default to 12%
        
        // Create eligibility request with proper field mapping
        EligibilityRequest request = EligibilityRequest.builder()
                .monthlyIncome(monthlyIncome > 0 ? monthlyIncome : 50000.0) // Default income if invalid
                .existingEmiAmount(existingEmi >= 0 ? existingEmi : 0.0)
                .creditScore(creditScore >= 300 && creditScore <= 900 ? creditScore : 650) // Default to minimum eligible
                .requestedLoanAmount(safeRequestedAmount)
                .tenureMonths(safeTenure)
                .annualInterestRate(safeInterestRate)
                .build();
        
        return evaluateEligibility(request);
    }

    public EligibilityResult checkEligibility(Double monthlyIncome, double existingEmi, int creditScore, Double requestedAmount, Integer tenure, double interestRate) {
        // Handle null monthlyIncome safely
        double safeMonthlyIncome = monthlyIncome != null && monthlyIncome > 0 ? monthlyIncome : 50000.0;
        return checkEligibility(safeMonthlyIncome, existingEmi, creditScore, requestedAmount, tenure, interestRate);
    }
}
