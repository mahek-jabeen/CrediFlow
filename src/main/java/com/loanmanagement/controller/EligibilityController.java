package com.loanmanagement.controller;

import com.loanmanagement.dto.EligibilityRequest;
import com.loanmanagement.dto.EligibilityResult;
import com.loanmanagement.service.EligibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eligibility")
@RequiredArgsConstructor
@Slf4j
public class EligibilityController {

    private final EligibilityService eligibilityService;

    @PostMapping("/check")
    public ResponseEntity<?> checkEligibility(@RequestBody EligibilityRequest request) {
        try {
            // Validate input
            if (request.getMonthlyIncome() <= 0 || request.getRequestedLoanAmount() <= 0) {
                return ResponseEntity.badRequest().body("Invalid input: income and loan amount must be positive");
            }
            
            log.info("Checking eligibility for loan amount: {}", request.getRequestedLoanAmount());
            EligibilityResult result = eligibilityService.evaluateEligibility(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking eligibility", e);
            return ResponseEntity.badRequest().body("Error processing eligibility check: " + e.getMessage());
        }
    }
}
