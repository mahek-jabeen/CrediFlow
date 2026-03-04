package com.loanmanagement.service;

import com.loanmanagement.dto.EmiScheduleItem;

import java.util.List;

public interface EmiCalculationService {

    /**
     * Calculates the monthly EMI (Equated Monthly Installment) using the standard formula.
     * 
     * Formula: EMI = P × r × (1 + r)^n / ((1 + r)^n - 1)
     * where:
     *   P = Principal loan amount
     *   r = Monthly interest rate (annual rate / 12 / 100)
     *   n = Tenure in months
     * 
     * @param principal the principal loan amount
     * @param annualInterestRate the annual interest rate in percentage
     * @param tenureMonths the loan tenure in months
     * @return the calculated monthly EMI amount rounded to two decimal places
     */
    double calculateMonthlyEmi(double principal, double annualInterestRate, int tenureMonths);

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
    List<EmiScheduleItem> generateEmiSchedule(double principal, double annualInterestRate, int tenureMonths);
}
