package com.loanmanagement.repository;

import com.loanmanagement.entity.LoanApplication;
import com.loanmanagement.entity.LoanApplication.ApplicationStatus;
import com.loanmanagement.entity.LoanApplication.LoanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    Optional<LoanApplication> findByApplicationNumber(String applicationNumber);

    List<LoanApplication> findByUserId(Long userId);

    List<LoanApplication> findByStatus(ApplicationStatus status);

    List<LoanApplication> findByLoanType(LoanType loanType);

    List<LoanApplication> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    List<LoanApplication> findByUserIdAndLoanType(Long userId, LoanType loanType);

    List<LoanApplication> findByUserIdOrderByApplicationDateDesc(Long userId);

    boolean existsByApplicationNumber(String applicationNumber);

    // Admin pagination and filtering
    @Query("""
SELECT la FROM LoanApplication la
JOIN FETCH la.user u
WHERE
(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
AND (:applicationNumber IS NULL OR LOWER(la.applicationNumber) LIKE LOWER(CONCAT('%', :applicationNumber, '%')))
AND (:status IS NULL OR la.status = :status)
AND (:loanType IS NULL OR la.loanType = :loanType)
AND (:dateFrom IS NULL OR la.applicationDate >= :dateFrom)
AND (:dateTo IS NULL OR la.applicationDate <= :dateTo)
AND (:minAmount IS NULL OR la.requestedAmount >= :minAmount)
AND (:maxAmount IS NULL OR la.requestedAmount <= :maxAmount)
""")
    Page<LoanApplication> findAdminLoansWithFilters(
            @Param("email") String email,
            @Param("applicationNumber") String applicationNumber,
            @Param("status") LoanApplication.ApplicationStatus status,
            @Param("loanType") LoanApplication.LoanType loanType,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount,
            Pageable pageable
    );
}
