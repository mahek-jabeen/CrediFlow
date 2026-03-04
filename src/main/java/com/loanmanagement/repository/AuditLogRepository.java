package com.loanmanagement.repository;

import com.loanmanagement.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    @Query("SELECT a FROM AuditLog a WHERE a.loanApplicationId = ?1 ORDER BY a.timestamp DESC")
    List<AuditLog> findByLoanApplicationIdOrderByTimestampDesc(Long loanApplicationId);
}
