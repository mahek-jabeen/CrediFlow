package com.loanmanagement.controller;

import com.loanmanagement.dto.LoanApplicationDto;
import com.loanmanagement.dto.LoanApprovalRequest;
import com.loanmanagement.dto.UserDto;
import com.loanmanagement.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserDto> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to fetch users: " + e.getMessage())
            );
        }
    }

    @GetMapping("/loan-applications")
    public ResponseEntity<?> getAllLoanApplications() {
        try {
            List<LoanApplicationDto> applications = adminService.getAllLoanApplications();
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to fetch loan applications: " + e.getMessage())
            );
        }
    }

    @PostMapping("/loan-approval")
    public ResponseEntity<?> processLoanApproval(@RequestBody LoanApprovalRequest request) {
        try {
            if ("APPROVE".equalsIgnoreCase(request.getAction())) {
                LoanApplicationDto approvedLoan = adminService.approveLoan(
                    request.getApplicationNumber(), 
                    "ADMIN" // In real app, get from authenticated user
                );
                return ResponseEntity.ok(approvedLoan);
            } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
                LoanApplicationDto rejectedLoan = adminService.rejectLoan(
                    request.getApplicationNumber(),
                    request.getRejectionReason(),
                    "ADMIN" // In real app, get from authenticated user
                );
                return ResponseEntity.ok(rejectedLoan);
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid action. Must be APPROVE or REJECT")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
}
