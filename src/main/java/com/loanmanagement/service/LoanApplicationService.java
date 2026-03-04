package com.loanmanagement.service;

import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.entity.LoanApplication.ApplicationStatus;
import com.loanmanagement.entity.LoanApplication.LoanType;

import java.util.List;
import java.util.Optional;

public interface LoanApplicationService {

    LoanApplication createLoanApplication(LoanApplication loanApplication);

    LoanApplication updateLoanApplication(Long id, LoanApplication loanApplication);

    LoanApplication updateApplicationStatus(Long id, ApplicationStatus status, String remarks);

    Optional<LoanApplication> getLoanApplicationById(Long id);

    Optional<LoanApplication> getLoanApplicationByApplicationNumber(String applicationNumber);

    List<LoanApplication> getAllLoanApplications();

    List<LoanApplication> getLoanApplicationsByUserId(Long userId);

    List<LoanApplication> getLoanApplicationsByStatus(ApplicationStatus status);

    List<LoanApplication> getLoanApplicationsByLoanType(LoanType loanType);

    void deleteLoanApplication(Long id);
}
