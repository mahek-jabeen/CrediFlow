package com.loanmanagement.controller;

import com.loanmanagement.dto.EmiScheduleItem;
import com.loanmanagement.dto.EmiScheduleRequest;
import com.loanmanagement.service.EmiCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/emi")
@RequiredArgsConstructor
@Slf4j
public class EmiController {

    private final EmiCalculationService emiCalculationService;

    /**
     * Generates an EMI amortization schedule based on the provided loan parameters.
     * 
     * @param request the EMI schedule request containing principal, interest rate, and tenure
     * @return a list of EmiScheduleItem objects representing the complete amortization schedule
     */
    @PostMapping("/schedule")
    public ResponseEntity<List<EmiScheduleItem>> generateEmiSchedule(@RequestBody EmiScheduleRequest request) {
        try {
            // Validate input
            if (request.getPrincipalAmount() <= 0 || request.getTenureMonths() <= 0 || request.getAnnualInterestRate() < 0) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            
            log.info("Received request to generate EMI schedule: {}", request);
            
            List<EmiScheduleItem> schedule = emiCalculationService.generateEmiSchedule(
                request.getPrincipalAmount(),
                request.getAnnualInterestRate(),
                request.getTenureMonths()
            );
            
            log.info("Generated EMI schedule with {} items", schedule.size());
            
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Error generating EMI schedule", e);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
}
