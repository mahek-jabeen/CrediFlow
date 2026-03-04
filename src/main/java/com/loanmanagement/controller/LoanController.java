package com.loanmanagement.controller;

import com.loanmanagement.dto.ApiResponse;
import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.Loan.LoanStatus;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<ApiResponse<Loan>> createLoan(@RequestBody Loan loan) {
        log.info("Creating new loan for user: {}", loan.getUser().getId());
        Loan createdLoan = loanService.createLoan(loan);
        return new ResponseEntity<>(ApiResponse.success("Loan created successfully", createdLoan), 
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Loan>> getLoanById(@PathVariable Long id) {
        log.info("Fetching loan with id: {}", id);
        Loan loan = loanService.getLoanById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));
        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Loan>>> getAllLoans() {
        log.info("Fetching all loans");
        List<Loan> loans = loanService.getAllLoans();
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/loan-number/{loanNumber}")
    public ResponseEntity<ApiResponse<Loan>> getLoanByLoanNumber(@PathVariable String loanNumber) {
        log.info("Fetching loan with number: {}", loanNumber);
        Loan loan = loanService.getLoanByLoanNumber(loanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "loanNumber", loanNumber));
        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Loan>>> getLoansByUserId(@PathVariable Long userId) {
        log.info("Fetching loans for user: {}", userId);
        List<Loan> loans = loanService.getLoansByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Loan>>> getLoansByStatus(@PathVariable LoanStatus status) {
        log.info("Fetching loans with status: {}", status);
        List<Loan> loans = loanService.getLoansByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<Loan>> getLoanByApplicationId(@PathVariable Long applicationId) {
        log.info("Fetching loan for application: {}", applicationId);
        Loan loan = loanService.getLoanByApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "applicationId", applicationId));
        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Loan>> updateLoan(@PathVariable Long id, @RequestBody Loan loan) {
        log.info("Updating loan with id: {}", id);
        loanService.getLoanById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));
        Loan updatedLoan = loanService.updateLoan(id, loan);
        return ResponseEntity.ok(ApiResponse.success("Loan updated successfully", updatedLoan));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Loan>> updateLoanStatus(
            @PathVariable Long id,
            @RequestParam LoanStatus status) {
        log.info("Updating status for loan id: {} to {}", id, status);
        Loan updated = loanService.updateLoanStatus(id, status);
        if (updated == null) {
            throw new ResourceNotFoundException("Loan", "id", id);
        }
        return ResponseEntity.ok(ApiResponse.success("Loan status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLoan(@PathVariable Long id) {
        log.info("Deleting loan with id: {}", id);
        loanService.getLoanById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));
        loanService.deleteLoan(id);
        return ResponseEntity.ok(ApiResponse.success("Loan deleted successfully", null));
    }
}
