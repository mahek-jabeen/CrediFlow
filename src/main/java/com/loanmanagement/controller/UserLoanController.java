package com.loanmanagement.controller;

import com.loanmanagement.dto.LoanApplicationRequest;
import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.entity.User;
import com.loanmanagement.repository.LoanApplicationRepository;
import com.loanmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/loan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserLoanController {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLoan(@RequestBody LoanApplicationRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create loan application
            LoanApplication application = new LoanApplication();
            application.setApplicationNumber(generateApplicationNumber());
            application.setUser(user);
            application.setRequestedAmount(request.getAmount());
            application.setRequestedTenureMonths(request.getTenure());
            application.setProposedInterestRate(request.getInterestRate());
            application.setStatus(LoanApplication.ApplicationStatus.PENDING);
            application.setApplicationDate(LocalDateTime.now());
            application.setCreatedAt(LocalDateTime.now());

            LoanApplication savedApplication = loanApplicationRepository.save(application);

            return ResponseEntity.ok(savedApplication);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getLoanStatus(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body(
                        java.util.Map.of("message", "UNAUTHORIZED")
                );
            }

            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.ok(
                        java.util.Map.of("data", java.util.List.of())
                );
            }

            List<LoanApplication> applications =
                    loanApplicationRepository.findByUserId(user.getId());

            if (applications == null || applications.isEmpty()) {
                return ResponseEntity.ok(
                        java.util.Map.of("data", java.util.List.of())
                );
            }

            return ResponseEntity.ok(
                    java.util.Map.of("data", applications)
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    java.util.Map.of("error", e.getMessage())
            );
        }
    }



    private String generateApplicationNumber() {
        return "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
