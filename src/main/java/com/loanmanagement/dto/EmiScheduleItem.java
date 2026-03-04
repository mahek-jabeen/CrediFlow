package com.loanmanagement.dto;

/**
 * Data Transfer Object representing a single EMI schedule item.
 * Contains details about an individual EMI payment including principal and interest breakdown.
 */
public class EmiScheduleItem {

    private int emiNumber;
    private double emiAmount;
    private double principalComponent;
    private double interestComponent;
    private double remainingPrincipal;

    public EmiScheduleItem() {
    }

    public EmiScheduleItem(int emiNumber, double emiAmount, double principalComponent, 
                           double interestComponent, double remainingPrincipal) {
        this.emiNumber = emiNumber;
        this.emiAmount = emiAmount;
        this.principalComponent = principalComponent;
        this.interestComponent = interestComponent;
        this.remainingPrincipal = remainingPrincipal;
    }

    public int getEmiNumber() {
        return emiNumber;
    }

    public void setEmiNumber(int emiNumber) {
        this.emiNumber = emiNumber;
    }

    public double getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(double emiAmount) {
        this.emiAmount = emiAmount;
    }

    public double getPrincipalComponent() {
        return principalComponent;
    }

    public void setPrincipalComponent(double principalComponent) {
        this.principalComponent = principalComponent;
    }

    public double getInterestComponent() {
        return interestComponent;
    }

    public void setInterestComponent(double interestComponent) {
        this.interestComponent = interestComponent;
    }

    public double getRemainingPrincipal() {
        return remainingPrincipal;
    }

    public void setRemainingPrincipal(double remainingPrincipal) {
        this.remainingPrincipal = remainingPrincipal;
    }

    @Override
    public String toString() {
        return "EmiScheduleItem{" +
                "emiNumber=" + emiNumber +
                ", emiAmount=" + emiAmount +
                ", principalComponent=" + principalComponent +
                ", interestComponent=" + interestComponent +
                ", remainingPrincipal=" + remainingPrincipal +
                '}';
    }
}
