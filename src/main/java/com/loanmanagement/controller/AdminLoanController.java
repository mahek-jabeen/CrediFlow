package com.loanmanagement.controller;

import com.loanmanagement.dto.AdminLoanListResponse;
import com.loanmanagement.dto.AdminLoanListPageResponse;
import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.repository.LoanApplicationRepository;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.service.AdminService;
import com.loanmanagement.service.AuditLogService;
import com.loanmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLoanController {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepository loanRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<?> getAllLoans(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String applicationNumber,
            @RequestParam(required = false) LoanApplication.ApplicationStatus status,
            @RequestParam(required = false) LoanApplication.LoanType loanType,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            // Convert empty strings to null for proper filtering
            String emailFilter = StringUtils.hasText(email) ? email : null;
            String applicationNumberFilter = StringUtils.hasText(applicationNumber) ? applicationNumber : null;
            
            // Convert invalid numeric values to null
            Double minAmountFilter = (minAmount != null && minAmount > 0) ? minAmount : null;
            Double maxAmountFilter = (maxAmount != null && maxAmount > 0) ? maxAmount : null;
            
            // Convert empty/invalid dates to null
            LocalDateTime dateFromFilter = null;
            LocalDateTime dateToFilter = null;

            try {
                if (StringUtils.hasText(dateFrom)) {
                    dateFromFilter = LocalDateTime.parse(dateFrom + "T00:00:00");
                }
                if (StringUtils.hasText(dateTo)) {
                    dateToFilter = LocalDateTime.parse(dateTo + "T23:59:59");
                }
            } catch (Exception e) {
                // ignore invalid date format safely
            }

            // Create pageable with sorting
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // Use admin service to get filtered loans
            Page<LoanApplication> loanPage = adminService.getFilteredLoans(
                emailFilter,
                applicationNumberFilter,
                status,
                loanType,
                dateFromFilter,
                dateToFilter,
                minAmountFilter,
                maxAmountFilter,
                pageable
            );

            // Convert to response format
            List<Map<String, Object>> content = loanPage.getContent().stream()
                    .map(l -> {
                        Map<String, Object> m = new java.util.HashMap<>();
                        m.put("id", l.getId());
                        m.put("applicationNumber", l.getApplicationNumber());
                        m.put("loanType", l.getLoanType());
                        m.put("requestedAmount", l.getRequestedAmount());
                        m.put("requestedTenureMonths", l.getRequestedTenureMonths());
                        m.put("status", l.getStatus());
                        m.put("applicationDate", l.getApplicationDate());
                        if (l.getUser() != null) {
                            m.put("userName", l.getUser().getFullName());
                            m.put("userEmail", l.getUser().getEmail());
                        } else {
                            m.put("userName", null);
                            m.put("userEmail", null);
                        }
                        return m;
                    })
                    .toList();

            // Create paginated response
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("content", content);
            response.put("page", loanPage.getNumber());
            response.put("size", loanPage.getSize());
            response.put("totalElements", loanPage.getTotalElements());
            response.put("totalPages", loanPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "content", List.of(),
                "page", 0,
                "size", size,
                "totalElements", 0L,
                "totalPages", 0
            )); // NEVER break UI
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getLoansPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String loanType,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String applicationNumber,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount
    ) {
        try {
            // Create pageable with sorting by applicationDate DESC
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applicationDate"));
            
            // Sanitize and parse parameters safely
            String emailFilter = (email == null || email.trim().isEmpty()) ? null : email.trim();
            String applicationNumberFilter = (applicationNumber == null || applicationNumber.trim().isEmpty()) ? null : applicationNumber.trim();
            
            LoanApplication.ApplicationStatus statusEnum = null;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    statusEnum = LoanApplication.ApplicationStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Invalid status: " + status);
                }
            }
            
            LoanApplication.LoanType loanTypeEnum = null;
            if (loanType != null && !loanType.trim().isEmpty()) {
                try {
                    loanTypeEnum = LoanApplication.LoanType.valueOf(loanType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Invalid loan type: " + loanType);
                }
            }
            
            LocalDateTime dateFromParsed = null;
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                try {
                    dateFromParsed = LocalDateTime.parse(dateFrom);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Invalid dateFrom format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
                }
            }
            
            LocalDateTime dateToParsed = null;
            if (dateTo != null && !dateTo.trim().isEmpty()) {
                try {
                    dateToParsed = LocalDateTime.parse(dateTo);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Invalid dateTo format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
                }
            }
            
            // Query with filters
            Page<LoanApplication> loanPage = loanApplicationRepository.findAdminLoansWithFilters(
                emailFilter,
                applicationNumberFilter,
                statusEnum,
                loanTypeEnum,
                dateFromParsed,
                dateToParsed,
                minAmount,
                maxAmount,
                pageable
            );
            
            // Convert to DTOs
            List<AdminLoanListResponse> loanDtos = loanPage.getContent().stream()
                .map(this::convertToAdminLoanListResponse)
                .collect(Collectors.toList());
            
            // Create response
            AdminLoanListPageResponse response = new AdminLoanListPageResponse();
            response.setLoans(loanDtos);
            response.setCurrentPage(loanPage.getNumber());
            response.setPageSize(loanPage.getSize());
            response.setTotalElements(loanPage.getTotalElements());
            response.setTotalPages(loanPage.getTotalPages());
            response.setHasNext(loanPage.hasNext());
            response.setHasPrevious(loanPage.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching paginated loans: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching paginated loans: " + e.getMessage());
        }
    }

    private AdminLoanListResponse convertToAdminLoanListResponse(LoanApplication loan) {
        AdminLoanListResponse response = new AdminLoanListResponse();
        response.setApplicationId(loan.getId());
        response.setApplicationNumber(loan.getApplicationNumber());
        response.setUserName(loan.getUser() != null ? loan.getUser().getFullName() : null);
        response.setUserEmail(loan.getUser() != null ? loan.getUser().getEmail() : null);
        response.setLoanType(loan.getLoanType() != null ? loan.getLoanType().toString() : null);
        response.setRequestedAmount(loan.getRequestedAmount());
        response.setTenure(loan.getRequestedTenureMonths());
        response.setStatus(loan.getStatus() != null ? loan.getStatus().toString() : null);
        response.setApplicationDate(loan.getApplicationDate());
        response.setReviewedDate(loan.getReviewedDate());
        return response;
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics() {
        try {
            List<LoanApplication> allLoans = loanApplicationRepository.findAll();
            
            // Initialize counters with safe defaults
            long totalApplications = 0;
            long pendingLoans = 0;
            long approvedLoans = 0;
            long rejectedLoans = 0;
            double totalLoanAmount = 0.0;

            if (allLoans != null) {
                totalApplications = allLoans.size();
                
                for (LoanApplication loan : allLoans) {
                    if (loan.getStatus() != null) {
                        switch (loan.getStatus()) {
                            case PENDING:
                                pendingLoans++;
                                break;
                            case APPROVED:
                                approvedLoans++;
                                break;
                            case REJECTED:
                                rejectedLoans++;
                                break;
                            default:
                                break;
                        }
                    }
                    
                    // Safely sum requested amounts
                    if (loan.getRequestedAmount() != null) {
                        totalLoanAmount += loan.getRequestedAmount();
                    }
                }
            }

            // Create response object
            Map<String, Object> metrics = new java.util.HashMap<>();
            metrics.put("totalApplications", totalApplications);
            metrics.put("pendingLoans", pendingLoans);
            metrics.put("approvedLoans", approvedLoans);
            metrics.put("rejectedLoans", rejectedLoans);
            metrics.put("totalLoanAmount", totalLoanAmount);

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            System.err.println("Error fetching metrics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching metrics: " + e.getMessage());
        }
    }

    @GetMapping("/export/csv")
    public ResponseEntity<?> exportCsv(HttpServletResponse response) {
        try {
            List<LoanApplication> loans = loanApplicationRepository.findAll();
            
            if (loans == null) {
                loans = new java.util.ArrayList<>();
            }

            // Set response headers
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"loan_applications.csv\"");

            // Create CSV content
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Application Number,User Name,Email,Loan Type,Amount,Tenure,Status,Applied Date\n");

            for (LoanApplication loan : loans) {
                csvContent.append("\"").append(loan.getApplicationNumber() != null ? loan.getApplicationNumber() : "").append("\",");
                csvContent.append("\"").append(loan.getUser() != null && loan.getUser().getFullName() != null ? loan.getUser().getFullName() : "").append("\",");
                csvContent.append("\"").append(loan.getUser() != null && loan.getUser().getEmail() != null ? loan.getUser().getEmail() : "").append("\",");
                csvContent.append("\"").append(loan.getLoanType() != null ? loan.getLoanType() : "").append("\",");
                csvContent.append("\"").append(loan.getRequestedAmount() != null ? loan.getRequestedAmount() : 0).append("\",");
                csvContent.append("\"").append(loan.getRequestedTenureMonths() != null ? loan.getRequestedTenureMonths() : 0).append("\",");
                csvContent.append("\"").append(loan.getStatus() != null ? loan.getStatus() : "").append("\",");
                csvContent.append("\"").append(loan.getApplicationDate() != null ? loan.getApplicationDate() : "").append("\"\n");
            }

            // Write to response
            response.getWriter().write(csvContent.toString());
            response.getWriter().flush();
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error exporting CSV: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error exporting CSV: " + e.getMessage());
        }
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<?> exportPdf(HttpServletResponse response) {
        try {
            List<LoanApplication> loans = loanApplicationRepository.findAll();
            
            if (loans == null) {
                loans = new java.util.ArrayList<>();
            }

            // Set response headers
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"loan_applications.pdf\"");

            // Create simple text-based PDF content
            StringBuilder pdfContent = new StringBuilder();
            pdfContent.append("Loan Applications Report\n");
            pdfContent.append("========================\n\n");

            for (LoanApplication loan : loans) {
                pdfContent.append("Application Number: ").append(loan.getApplicationNumber() != null ? loan.getApplicationNumber() : "").append("\n");
                pdfContent.append("User Name: ").append(loan.getUser() != null && loan.getUser().getFullName() != null ? loan.getUser().getFullName() : "").append("\n");
                pdfContent.append("Email: ").append(loan.getUser() != null && loan.getUser().getEmail() != null ? loan.getUser().getEmail() : "").append("\n");
                pdfContent.append("Loan Type: ").append(loan.getLoanType() != null ? loan.getLoanType() : "").append("\n");
                pdfContent.append("Amount: $").append(loan.getRequestedAmount() != null ? loan.getRequestedAmount() : 0).append("\n");
                pdfContent.append("Tenure: ").append(loan.getRequestedTenureMonths() != null ? loan.getRequestedTenureMonths() : 0).append(" months\n");
                pdfContent.append("Status: ").append(loan.getStatus() != null ? loan.getStatus() : "").append("\n");
                pdfContent.append("Applied Date: ").append(loan.getApplicationDate() != null ? loan.getApplicationDate() : "").append("\n");
                pdfContent.append("------------------------\n\n");
            }

            // Write to response as plain text (fallback PDF)
            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment; filename=\"loan_applications.txt\"");
            response.getWriter().write(pdfContent.toString());
            response.getWriter().flush();
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error exporting PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error exporting PDF: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLoanById(@PathVariable Long id) {
        try {
            LoanApplication loanApplication = loanApplicationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Loan application not found"));

            // Null safety checks
            if (loanApplication.getUser() == null) {
                return ResponseEntity.badRequest().body("Error: User information is missing");
            }

            // Create safe response object
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", loanApplication.getId());
            response.put("applicationNumber", loanApplication.getApplicationNumber());
            response.put("loanType", loanApplication.getLoanType());
            response.put("requestedAmount", loanApplication.getRequestedAmount());
            response.put("requestedTenureMonths", loanApplication.getRequestedTenureMonths());
            response.put("proposedInterestRate", loanApplication.getProposedInterestRate());
            response.put("creditScore", loanApplication.getCreditScore());
            response.put("existingEmiAmount", loanApplication.getExistingEmiAmount());
            response.put("status", loanApplication.getStatus());
            response.put("remarks", loanApplication.getRemarks());
            response.put("applicationDate", loanApplication.getApplicationDate());
            response.put("reviewedDate", loanApplication.getReviewedDate());
            response.put("reviewedBy", loanApplication.getReviewedBy());
            response.put("userName", loanApplication.getUser().getFullName());
            response.put("userEmail", loanApplication.getUser().getEmail());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching loan details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching loan details: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLoan(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        try {
            LoanApplication loanApplication = loanApplicationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Loan application not found"));

            // Null safety checks
            if (loanApplication.getUser() == null) {
                return ResponseEntity.badRequest().body("Error: User information is missing");
            }

            // Status validation
            if (loanApplication.getStatus() == LoanApplication.ApplicationStatus.APPROVED) {
                return ResponseEntity.badRequest().body("Error: Loan is already approved");
            }
            if (loanApplication.getStatus() == LoanApplication.ApplicationStatus.REJECTED) {
                return ResponseEntity.badRequest().body("Error: Loan is already rejected");
            }

            LoanApplication.ApplicationStatus oldStatus = loanApplication.getStatus();
            loanApplication.setStatus(LoanApplication.ApplicationStatus.APPROVED);
            loanApplication.setReviewedDate(LocalDateTime.now());
            loanApplication.setReviewedBy("ADMIN");
            
            // Add remarks if provided
            if (request != null && request.containsKey("remarks")) {
                loanApplication.setRemarks(request.get("remarks"));
            }

            loanApplicationRepository.save(loanApplication);

            // Create audit log
            auditLogService.createAuditLog(
                com.loanmanagement.entity.AuditLog.AuditAction.APPROVED_BY_ADMIN,
                id,
                "ADMIN",
                oldStatus,
                LoanApplication.ApplicationStatus.APPROVED,
                loanApplication.getRemarks()
            );

            // Create notification for user
            if (loanApplication.getUser() != null) {
                notificationService.createLoanStatusNotification(loanApplication.getUser(), loanApplication);
            }

            // Create loan record only if user exists
            try {
                Loan loan = new Loan();
                loan.setLoanNumber(generateLoanNumber());
                loan.setUser(loanApplication.getUser());
                loan.setPrincipalAmount(loanApplication.getRequestedAmount());
                loan.setInterestRate(loanApplication.getProposedInterestRate());
                loan.setTenureMonths(loanApplication.getRequestedTenureMonths());
                loan.setStatus(Loan.LoanStatus.ACTIVE);
                loan.setDisbursementDate(LocalDateTime.now().toLocalDate());
                
                // Calculate end date
                loan.setFirstEmiDate(LocalDateTime.now().toLocalDate().plusMonths(1));
                
                loanRepository.save(loan);
            } catch (Exception loanCreateError) {
                // Log but don't fail the approval
                System.err.println("Warning: Failed to create loan record: " + loanCreateError.getMessage());
            }

            return ResponseEntity.ok("Loan approved successfully");
        } catch (Exception e) {
            System.err.println("Error approving loan: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error approving loan: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLoan(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            LoanApplication loanApplication = loanApplicationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Loan application not found"));

            // Null safety checks
            if (loanApplication.getUser() == null) {
                return ResponseEntity.badRequest().body("Error: User information is missing");
            }

            // Status validation
            if (loanApplication.getStatus() == LoanApplication.ApplicationStatus.APPROVED) {
                return ResponseEntity.badRequest().body("Error: Loan is already approved");
            }
            if (loanApplication.getStatus() == LoanApplication.ApplicationStatus.REJECTED) {
                return ResponseEntity.badRequest().body("Error: Loan is already rejected");
            }

            // Validate remarks for rejection
            String remarks = "";
            if (request != null && request.containsKey("remarks")) {
                remarks = request.get("remarks");
            }
            
            if (remarks.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Rejection reason is required");
            }

            LoanApplication.ApplicationStatus oldStatus = loanApplication.getStatus();
            loanApplication.setStatus(LoanApplication.ApplicationStatus.REJECTED);
            loanApplication.setRemarks(remarks);
            loanApplication.setReviewedDate(LocalDateTime.now());
            loanApplication.setReviewedBy("ADMIN");

            loanApplicationRepository.save(loanApplication);

            // Create audit log
            auditLogService.createAuditLog(
                com.loanmanagement.entity.AuditLog.AuditAction.REJECTED_BY_ADMIN,
                id,
                "ADMIN",
                oldStatus,
                LoanApplication.ApplicationStatus.REJECTED,
                remarks
            );

            // Create notification for user
            if (loanApplication.getUser() != null) {
                notificationService.createLoanStatusNotification(loanApplication.getUser(), loanApplication);
            }
            return ResponseEntity.ok("Loan rejected successfully");
        } catch (Exception e) {
            System.err.println("Error rejecting loan: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error rejecting loan: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/under-review")
    public ResponseEntity<?> markUnderReview(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            LoanApplication loanApplication = loanApplicationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Loan application not found"));

            // Null safety checks
            if (loanApplication.getUser() == null) {
                return ResponseEntity.badRequest().body("Error: User information is missing");
            }

            // Status validation
            if (loanApplication.getStatus() == LoanApplication.ApplicationStatus.APPROVED) {
                return ResponseEntity.badRequest().body("Error: Loan is already approved");
            }
            if (loanApplication.getStatus() == LoanApplication.ApplicationStatus.REJECTED) {
                return ResponseEntity.badRequest().body("Error: Loan is already rejected");
            }

            LoanApplication.ApplicationStatus oldStatus = loanApplication.getStatus();
            loanApplication.setStatus(LoanApplication.ApplicationStatus.UNDER_REVIEW);
            loanApplication.setReviewedDate(LocalDateTime.now());
            loanApplication.setReviewedBy("ADMIN");
            
            // Add remarks if provided
            if (request != null && request.containsKey("remarks")) {
                loanApplication.setRemarks(request.get("remarks"));
            }

            loanApplicationRepository.save(loanApplication);

            // Create audit log
            auditLogService.createAuditLog(
                com.loanmanagement.entity.AuditLog.AuditAction.MARKED_UNDER_REVIEW,
                id,
                "ADMIN",
                oldStatus,
                LoanApplication.ApplicationStatus.UNDER_REVIEW,
                loanApplication.getRemarks()
            );

            // Create notification for user
            if (loanApplication.getUser() != null) {
                notificationService.createLoanStatusNotification(loanApplication.getUser(), loanApplication);
            }

            return ResponseEntity.ok("Loan marked under review successfully");
        } catch (Exception e) {
            System.err.println("Error marking loan under review: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error marking loan under review: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<?> getAuditLogs(@PathVariable Long id) {
        try {
            // Use service instead of repository
            List<Map<String, Object>> auditLogs = auditLogService.getLogsByLoanApplicationId(id);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            System.err.println("Error fetching audit logs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching audit logs: " + e.getMessage());
        }
    }

    private String generateLoanNumber() {
        return "LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
