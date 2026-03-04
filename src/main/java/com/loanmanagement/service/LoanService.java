package com.loanmanagement.service;

import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.Loan.LoanStatus;

import java.util.List;
import java.util.Optional;

public interface LoanService {

    Loan createLoan(Loan loan);

    Loan updateLoan(Long id, Loan loan);

    Loan updateLoanStatus(Long id, LoanStatus status);

    Optional<Loan> getLoanById(Long id);

    Optional<Loan> getLoanByLoanNumber(String loanNumber);

    Optional<Loan> getLoanByApplicationId(Long applicationId);

    List<Loan> getAllLoans();

    List<Loan> getLoansByUserId(Long userId);

    List<Loan> getLoansByStatus(LoanStatus status);

    void deleteLoan(Long id);
}
