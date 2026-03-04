package com.loanmanagement.service.impl;

import com.loanmanagement.dto.EmiScheduleItem;
import com.loanmanagement.service.EmiCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EmiCalculationServiceImpl implements EmiCalculationService {

    /**
     * Calculates the monthly EMI (Equated Monthly Installment) using the standard formula.
     * 
     * Formula: EMI = P × r × (1 + r)^n / ((1 + r)^n - 1)
     * where:
     *   P = Principal loan amount
     *   r = Monthly interest rate (annual rate / 12 / 100)
     *   n = Tenure in months
     * 
     * Special case: If interest rate is 0, EMI = P / n
     * 
     * @param principal the principal loan amount
     * @param annualInterestRate the annual interest rate in percentage
     * @param tenureMonths the loan tenure in months
     * @return the calculated monthly EMI amount rounded to two decimal places
     */
    @Override
    public double calculateMonthlyEmi(double principal, double annualInterestRate, int tenureMonths) {
        log.debug("Calculating EMI for principal: {}, annualInterestRate: {}%, tenure: {} months", 
                  principal, annualInterestRate, tenureMonths);
        
        // Handle special case: zero interest rate
        if (annualInterestRate == 0) {
            double emi = principal / tenureMonths;
            return roundToTwoDecimals(emi);
        }
        
        // Convert annual interest rate to monthly interest rate (in decimal)
        double monthlyInterestRate = annualInterestRate / 12 / 100;
        
        // Calculate (1 + r)^n
        double onePlusRPowerN = Math.pow(1 + monthlyInterestRate, tenureMonths);
        
        // Apply EMI formula: EMI = P × r × (1 + r)^n / ((1 + r)^n - 1)
        double emi = (principal * monthlyInterestRate * onePlusRPowerN) / (onePlusRPowerN - 1);
        
        double roundedEmi = roundToTwoDecimals(emi);
        
        log.debug("Calculated EMI: {}", roundedEmi);
        
        return roundedEmi;
    }
    
    /**
     * Generates a complete EMI amortization schedule showing the breakdown of each payment.
     * 
     * For each month, calculates:
     * - Interest component = Remaining Principal × Monthly Interest Rate
     * - Principal component = EMI - Interest component
     * - New remaining principal = Previous remaining principal - Principal component
     * 
     * All monetary values are rounded to two decimal places.
     * 
     * @param principal the principal loan amount
     * @param annualInterestRate the annual interest rate in percentage
     * @param tenureMonths the loan tenure in months
     * @return a list of EmiScheduleItem objects representing the complete amortization schedule
     */
    @Override
    public List<EmiScheduleItem> generateEmiSchedule(double principal, double annualInterestRate, int tenureMonths) {
        log.debug("Generating EMI schedule for principal: {}, annualInterestRate: {}%, tenure: {} months", 
                  principal, annualInterestRate, tenureMonths);
        
        List<EmiScheduleItem> schedule = new ArrayList<>();
        
        // Calculate the fixed monthly EMI
        double monthlyEmi = calculateMonthlyEmi(principal, annualInterestRate, tenureMonths);
        
        // Convert annual interest rate to monthly interest rate (in decimal)
        double monthlyInterestRate = annualInterestRate / 12 / 100;
        
        // Track the remaining principal balance
        double remainingPrincipal = principal;
        
        // Generate schedule for each month
        for (int month = 1; month <= tenureMonths; month++) {
            // Calculate interest component for this month
            double interestComponent = roundToTwoDecimals(remainingPrincipal * monthlyInterestRate);
            
            // Calculate principal component for this month
            double principalComponent = roundToTwoDecimals(monthlyEmi - interestComponent);
            
            // Update remaining principal
            remainingPrincipal = roundToTwoDecimals(remainingPrincipal - principalComponent);
            
            // Handle last month to ensure remaining principal is exactly zero
            if (month == tenureMonths && remainingPrincipal != 0) {
                // Adjust the principal component to account for rounding differences
                principalComponent = roundToTwoDecimals(principalComponent + remainingPrincipal);
                remainingPrincipal = 0.0;
            }
            
            // Create the EMI schedule item
            EmiScheduleItem item = new EmiScheduleItem(
                month,
                monthlyEmi,
                principalComponent,
                interestComponent,
                remainingPrincipal
            );
            
            schedule.add(item);
            
            log.trace("Month {}: EMI={}, Principal={}, Interest={}, Remaining={}", 
                     month, monthlyEmi, principalComponent, interestComponent, remainingPrincipal);
        }
        
        log.debug("Generated EMI schedule with {} items", schedule.size());
        
        return schedule;
    }
    
    /**
     * Rounds a double value to two decimal places using HALF_UP rounding mode.
     * 
     * @param value the value to round
     * @return the rounded value
     */
    private double roundToTwoDecimals(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
