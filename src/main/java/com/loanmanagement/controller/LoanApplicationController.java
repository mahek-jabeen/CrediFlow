package com.loanmanagement.controller;

import com.loanmanagement.dto.ApiResponse;
import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.entity.LoanApplication.ApplicationStatus;
import com.loanmanagement.entity.LoanApplication.LoanType;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-applications")
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanApplication>> createLoanApplication(
            @RequestBody LoanApplication loanApplication) {
        log.info("Creating new loan application for user: {}", loanApplication.getUser().getId());
        LoanApplication created = loanApplicationService.createLoanApplication(loanApplication);
        return new ResponseEntity<>(ApiResponse.success("Loan application created successfully", created), 
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanApplication>> getLoanApplicationById(@PathVariable Long id) {
        log.info("Fetching loan application with id: {}", id);
        LoanApplication application = loanApplicationService.getLoanApplicationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", id));
        return ResponseEntity.ok(ApiResponse.success(application));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getAllLoanApplications() {
        log.info("Fetching all loan applications");
        List<LoanApplication> applications = loanApplicationService.getAllLoanApplications();
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/application-number/{applicationNumber}")
    public ResponseEntity<ApiResponse<LoanApplication>> getLoanApplicationByNumber(
            @PathVariable String applicationNumber) {
        log.info("Fetching loan application with number: {}", applicationNumber);
        LoanApplication application = loanApplicationService
                .getLoanApplicationByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "applicationNumber", 
                        applicationNumber));
        return ResponseEntity.ok(ApiResponse.success(application));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getLoanApplicationsByUserId(
            @PathVariable Long userId) {
        log.info("Fetching loan applications for user: {}", userId);
        List<LoanApplication> applications = loanApplicationService.getLoanApplicationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getLoanApplicationsByStatus(
            @PathVariable ApplicationStatus status) {
        log.info("Fetching loan applications with status: {}", status);
        List<LoanApplication> applications = loanApplicationService.getLoanApplicationsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/loan-type/{loanType}")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getLoanApplicationsByLoanType(
            @PathVariable LoanType loanType) {
        log.info("Fetching loan applications with type: {}", loanType);
        List<LoanApplication> applications = loanApplicationService.getLoanApplicationsByLoanType(loanType);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanApplication>> updateLoanApplication(
            @PathVariable Long id, @RequestBody LoanApplication loanApplication) {
        log.info("Updating loan application with id: {}", id);
        loanApplicationService.getLoanApplicationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", id));
        LoanApplication updated = loanApplicationService.updateLoanApplication(id, loanApplication);
        return ResponseEntity.ok(ApiResponse.success("Loan application updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LoanApplication>> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String remarks) {
        log.info("Updating status for loan application id: {} to {}", id, status);
        LoanApplication updated = loanApplicationService.updateApplicationStatus(id, status, remarks);
        if (updated == null) {
            throw new ResourceNotFoundException("LoanApplication", "id", id);
        }
        return ResponseEntity.ok(ApiResponse.success("Application status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLoanApplication(@PathVariable Long id) {
        log.info("Deleting loan application with id: {}", id);
        loanApplicationService.getLoanApplicationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", id));
        loanApplicationService.deleteLoanApplication(id);
        return ResponseEntity.ok(ApiResponse.success("Loan application deleted successfully", null));
    }
}
