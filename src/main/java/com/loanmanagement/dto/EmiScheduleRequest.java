package com.loanmanagement.dto;

/**
 * Data Transfer Object for EMI schedule request.
 * Contains the loan parameters needed to generate an EMI schedule.
 */
public class EmiScheduleRequest {

    private double principalAmount;
    private double annualInterestRate;
    private int tenureMonths;

    public EmiScheduleRequest() {
    }

    public EmiScheduleRequest(double principalAmount, double annualInterestRate, int tenureMonths) {
        this.principalAmount = principalAmount;
        this.annualInterestRate = annualInterestRate;
        this.tenureMonths = tenureMonths;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(double annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(int tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    @Override
    public String toString() {
        return "EmiScheduleRequest{" +
                "principalAmount=" + principalAmount +
                ", annualInterestRate=" + annualInterestRate +
                ", tenureMonths=" + tenureMonths +
                '}';
    }
}
