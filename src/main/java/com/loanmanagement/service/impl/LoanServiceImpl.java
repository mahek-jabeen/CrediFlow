package com.loanmanagement.service.impl;

import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.Loan.LoanStatus;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    @Override
    public Loan createLoan(Loan loan) {
        log.debug("Creating loan for user: {}", loan.getUser().getId());
        return loanRepository.save(loan);
    }

    @Override
    public Loan updateLoan(Long id, Loan loan) {
        log.debug("Updating loan with id: {}", id);
        loan.setId(id);
        return loanRepository.save(loan);
    }

    @Override
    public Loan updateLoanStatus(Long id, LoanStatus status) {
        log.debug("Updating loan status for id: {} to {}", id, status);
        Optional<Loan> optionalLoan = loanRepository.findById(id);
        if (optionalLoan.isPresent()) {
            Loan loan = optionalLoan.get();
            loan.setStatus(status);
            return loanRepository.save(loan);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Loan> getLoanById(Long id) {
        log.debug("Fetching loan with id: {}", id);
        return loanRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Loan> getLoanByLoanNumber(String loanNumber) {
        log.debug("Fetching loan with number: {}", loanNumber);
        return loanRepository.findByLoanNumber(loanNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Loan> getLoanByApplicationId(Long applicationId) {
        log.debug("Fetching loan for application: {}", applicationId);
        return loanRepository.findByLoanApplicationId(applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        log.debug("Fetching all loans");
        return loanRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getLoansByUserId(Long userId) {
        log.debug("Fetching loans for user: {}", userId);
        return loanRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getLoansByStatus(LoanStatus status) {
        log.debug("Fetching loans with status: {}", status);
        return loanRepository.findByStatus(status);
    }

    @Override
    public void deleteLoan(Long id) {
        log.debug("Deleting loan with id: {}", id);
        loanRepository.deleteById(id);
    }
}
