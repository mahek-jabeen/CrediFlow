package com.loanmanagement.service;

import com.loanmanagement.dto.LoanApplicationDto;
import com.loanmanagement.dto.LoanApprovalRequest;
import com.loanmanagement.dto.UserDto;
import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.entity.User;
import com.loanmanagement.repository.LoanApplicationRepository;
import com.loanmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final EligibilityService eligibilityService;
    private final EmiService emiService;

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        return users.stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole() != null ? user.getRole().toString() : "USER");
            dto.setCreatedAt(user.getCreatedAt());
            
            // Count loan applications for this user
            try {
                Integer loanCount = loanApplicationRepository.findByUserId(user.getId()).size();
                dto.setLoanApplicationCount(loanCount);
            } catch (Exception e) {
                dto.setLoanApplicationCount(0);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    public List<LoanApplicationDto> getAllLoanApplications() {
        List<LoanApplication> applications = loanApplicationRepository.findAll();
        
        return applications.stream().map(app -> {
            LoanApplicationDto dto = new LoanApplicationDto();
            dto.setId(app.getId());
            dto.setApplicationNumber(app.getApplicationNumber());
            
            // Handle null user safely
            if (app.getUser() != null) {
                dto.setUserName(app.getUser().getFullName());
                dto.setUserEmail(app.getUser().getEmail());
                dto.setUserId(app.getUser().getId());
            } else {
                dto.setUserName("Unknown User");
                dto.setUserEmail("unknown@example.com");
                dto.setUserId(0L);
            }
            
            dto.setLoanType(app.getLoanType());
            dto.setRequestedAmount(app.getRequestedAmount());
            dto.setRequestedTenureMonths(app.getRequestedTenureMonths());
            dto.setProposedInterestRate(app.getProposedInterestRate());
            dto.setStatus(app.getStatus());
            dto.setRemarks(app.getRemarks());
            dto.setCreditScore(app.getCreditScore());
            dto.setExistingEmiAmount(app.getExistingEmiAmount());
            dto.setApplicationDate(app.getApplicationDate());
            dto.setReviewedDate(app.getReviewedDate());
            dto.setReviewedBy(app.getReviewedBy());
            
            // Check eligibility with null safety
            try {
                // Get user monthly income with safe default
                Double userMonthlyIncome = 0.0;
                if (app.getUser() != null && app.getUser().getMonthlyIncome() != null && app.getUser().getMonthlyIncome() > 0) {
                    userMonthlyIncome = app.getUser().getMonthlyIncome();
                } else {
                    log.warn("User monthly income is null or zero for application {}, using default", app.getApplicationNumber());
                    userMonthlyIncome = 50000.0; // Safe default income
                }
                
                boolean isEligible = eligibilityService.checkEligibility(
                    userMonthlyIncome,
                    app.getExistingEmiAmount() != null ? app.getExistingEmiAmount() : 0.0,
                    app.getCreditScore() != null ? app.getCreditScore() : 650,
                    app.getRequestedAmount(),
                    app.getRequestedTenureMonths(),
                    app.getProposedInterestRate() != null ? app.getProposedInterestRate() : 12.0
                ).isEligible();
                dto.setEligible(isEligible);
            } catch (Exception e) {
                log.error("Error checking eligibility for application {}: {}", app.getApplicationNumber(), e.getMessage());
                dto.setEligible(false);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    public Page<LoanApplication> getFilteredLoans(
            String email,
            String applicationNumber,
            LoanApplication.ApplicationStatus status,
            LoanApplication.LoanType loanType,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Double minAmount,
            Double maxAmount,
            Pageable pageable
    ) {
        return loanApplicationRepository.findAdminLoansWithFilters(
                email,
                applicationNumber,
                status,
                loanType,
                dateFrom,
                dateTo,
                minAmount,
                maxAmount,
                pageable
        );
    }

    public LoanApplicationDto approveLoan(String applicationNumber, String reviewedBy) {
        LoanApplication application = loanApplicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new RuntimeException("Loan application not found"));

        // Get user monthly income with safe default
        Double userMonthlyIncome = 0.0;
        if (application.getUser() != null && application.getUser().getMonthlyIncome() != null && application.getUser().getMonthlyIncome() > 0) {
            userMonthlyIncome = application.getUser().getMonthlyIncome();
        } else {
            log.warn("User monthly income is null or zero for application {}, using default", applicationNumber);
            userMonthlyIncome = 50000.0; // Safe default income
        }

        // Check eligibility before approval
        boolean isEligible = eligibilityService.checkEligibility(
            userMonthlyIncome,
            application.getExistingEmiAmount() != null ? application.getExistingEmiAmount() : 0.0,
            application.getCreditScore() != null ? application.getCreditScore() : 650,
            application.getRequestedAmount(),
            application.getRequestedTenureMonths(),
            application.getProposedInterestRate() != null ? application.getProposedInterestRate() : 12.0
        ).isEligible();

        if (!isEligible) {
            throw new RuntimeException("Cannot approve loan: Applicant is not eligible");
        }

        // Update application status
        application.setStatus(LoanApplication.ApplicationStatus.APPROVED);
        application.setReviewedDate(java.time.LocalDateTime.now());
        application.setReviewedBy(reviewedBy);

        // Generate loan using existing EMI service
        emiService.generateLoanFromApplication(application);

        loanApplicationRepository.save(application);

        // Return updated DTO
        return convertToDto(application);
    }

    public LoanApplicationDto rejectLoan(String applicationNumber, String rejectionReason, String reviewedBy) {
        LoanApplication application = loanApplicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new RuntimeException("Loan application not found"));

        // Update application status
        application.setStatus(LoanApplication.ApplicationStatus.REJECTED);
        application.setRemarks(rejectionReason);
        application.setReviewedDate(java.time.LocalDateTime.now());
        application.setReviewedBy(reviewedBy);

        loanApplicationRepository.save(application);

        // Return updated DTO
        return convertToDto(application);
    }

    private LoanApplicationDto convertToDto(LoanApplication application) {
        LoanApplicationDto dto = new LoanApplicationDto();
        dto.setId(application.getId());
        dto.setApplicationNumber(application.getApplicationNumber());
        dto.setUserName(application.getUser().getFullName());
        dto.setUserEmail(application.getUser().getEmail());
        dto.setUserId(application.getUser().getId());
        dto.setLoanType(application.getLoanType());
        dto.setRequestedAmount(application.getRequestedAmount());
        dto.setRequestedTenureMonths(application.getRequestedTenureMonths());
        dto.setProposedInterestRate(application.getProposedInterestRate());
        dto.setStatus(application.getStatus());
        dto.setRemarks(application.getRemarks());
        dto.setCreditScore(application.getCreditScore());
        dto.setExistingEmiAmount(application.getExistingEmiAmount());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setReviewedDate(application.getReviewedDate());
        dto.setReviewedBy(application.getReviewedBy());
        
        // Check eligibility with null safety
        try {
            // Get user monthly income with safe default
            Double userMonthlyIncome = 0.0;
            if (application.getUser() != null && application.getUser().getMonthlyIncome() != null && application.getUser().getMonthlyIncome() > 0) {
                userMonthlyIncome = application.getUser().getMonthlyIncome();
            } else {
                log.warn("User monthly income is null or zero for application {}, using default", application.getApplicationNumber());
                userMonthlyIncome = 50000.0; // Safe default income
            }
            
            boolean isEligible = eligibilityService.checkEligibility(
                userMonthlyIncome,
                application.getExistingEmiAmount() != null ? application.getExistingEmiAmount() : 0.0,
                application.getCreditScore() != null ? application.getCreditScore() : 650,
                application.getRequestedAmount(),
                application.getRequestedTenureMonths(),
                application.getProposedInterestRate() != null ? application.getProposedInterestRate() : 12.0
            ).isEligible();
            dto.setEligible(isEligible);
        } catch (Exception e) {
            log.error("Error checking eligibility for application {}: {}", application.getApplicationNumber(), e.getMessage());
            dto.setEligible(false);
        }
        
        return dto;
    }
}
