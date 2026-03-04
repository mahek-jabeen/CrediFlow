package com.loanmanagement.service;

import com.loanmanagement.entity.AuditLog;
import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    
    public void createAuditLog(AuditLog.AuditAction action, Long loanApplicationId, 
                            String adminEmail, LoanApplication.ApplicationStatus oldStatus, 
                            LoanApplication.ApplicationStatus newStatus, String remarks) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setLoanApplicationId(loanApplicationId);
            auditLog.setAdminEmail(adminEmail);
            auditLog.setOldStatus(oldStatus);
            auditLog.setNewStatus(newStatus);
            auditLog.setRemarks(remarks);

            auditLogRepository.save(auditLog);
            log.debug("Audit log created for loan application {}: {} -> {}", loanApplicationId, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("Failed to create audit log for loan application {}: {}", loanApplicationId, e.getMessage());
            // Don't throw - audit log failure shouldn't break main flow
        }
    }
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLogsByLoanApplicationId(Long loanApplicationId) {
        return auditLogRepository
                .findByLoanApplicationIdOrderByTimestampDesc(loanApplicationId)
                .stream()
                .map(log -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("action", log.getAction());
                    map.put("adminEmail", log.getAdminEmail());
                    map.put("oldStatus", log.getOldStatus());
                    map.put("newStatus", log.getNewStatus());
                    map.put("remarks", log.getRemarks());
                    map.put("timestamp", log.getTimestamp());
                    return map;
                })
                .toList();
    }

}
