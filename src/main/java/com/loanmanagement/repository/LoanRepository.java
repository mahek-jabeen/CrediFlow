package com.loanmanagement.repository;

import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.Loan.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    List<Loan> findByUserId(Long userId);

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);

    Optional<Loan> findByLoanApplicationId(Long loanApplicationId);

    boolean existsByLoanNumber(String loanNumber);
}
