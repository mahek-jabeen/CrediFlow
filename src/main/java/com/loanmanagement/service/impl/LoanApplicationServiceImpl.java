package com.loanmanagement.service.impl;

import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.entity.LoanApplication.ApplicationStatus;
import com.loanmanagement.entity.LoanApplication.LoanType;
import com.loanmanagement.repository.LoanApplicationRepository;
import com.loanmanagement.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;

    @Override
    public LoanApplication createLoanApplication(LoanApplication loanApplication) {

        log.debug("Creating loan application for user: {}", loanApplication.getUser().getId());

        // HARD ENFORCE — LAST LINE OF DEFENSE
        if (loanApplication.getLoanType() == null) {
            loanApplication.setLoanType(LoanType.PERSONAL_LOAN);
        }

        if (loanApplication.getStatus() == null) {
            loanApplication.setStatus(ApplicationStatus.PENDING);
        }

        if (loanApplication.getApplicationDate() == null) {
            loanApplication.setApplicationDate(LocalDateTime.now());
        }

        if (loanApplication.getApplicationNumber() == null) {
            loanApplication.setApplicationNumber("APP-" + System.currentTimeMillis());
        }

        if (loanApplication.getCreatedAt() == null) {
            loanApplication.setCreatedAt(LocalDateTime.now());
        }

        if (loanApplication.getUpdatedAt() == null) {
            loanApplication.setUpdatedAt(LocalDateTime.now());
        }

        log.info("FINAL loanType before save = {}", loanApplication.getLoanType());

        return loanApplicationRepository.saveAndFlush(loanApplication);
    }

    @Override
    public LoanApplication updateLoanApplication(Long id, LoanApplication loanApplication) {
        log.debug("Updating loan application with id: {}", id);
        loanApplication.setId(id);
        return loanApplicationRepository.save(loanApplication);
    }

    @Override
    public LoanApplication updateApplicationStatus(Long id, ApplicationStatus status, String remarks) {
        log.debug("Updating application status for id: {} to {}", id, status);
        Optional<LoanApplication> optionalApplication = loanApplicationRepository.findById(id);
        if (optionalApplication.isPresent()) {
            LoanApplication application = optionalApplication.get();
            application.setStatus(status);
            application.setRemarks(remarks);
            application.setReviewedDate(LocalDateTime.now());
            return loanApplicationRepository.save(application);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LoanApplication> getLoanApplicationById(Long id) {
        log.debug("Fetching loan application with id: {}", id);
        return loanApplicationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LoanApplication> getLoanApplicationByApplicationNumber(String applicationNumber) {
        log.debug("Fetching loan application with number: {}", applicationNumber);
        return loanApplicationRepository.findByApplicationNumber(applicationNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplication> getAllLoanApplications() {
        log.debug("Fetching all loan applications");
        return loanApplicationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplication> getLoanApplicationsByUserId(Long userId) {
        log.debug("Fetching loan applications for user: {}", userId);
        return loanApplicationRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplication> getLoanApplicationsByStatus(ApplicationStatus status) {
        log.debug("Fetching loan applications with status: {}", status);
        return loanApplicationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplication> getLoanApplicationsByLoanType(LoanType loanType) {
        log.debug("Fetching loan applications with type: {}", loanType);
        return loanApplicationRepository.findByLoanType(loanType);
    }

    @Override
    public void deleteLoanApplication(Long id) {
        log.debug("Deleting loan application with id: {}", id);
        loanApplicationRepository.deleteById(id);
    }
}
