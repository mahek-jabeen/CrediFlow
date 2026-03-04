package com.loanmanagement.repository;

import com.loanmanagement.entity.EmiSchedule;
import com.loanmanagement.entity.EmiSchedule.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {

    List<EmiSchedule> findByLoanId(Long loanId);

    List<EmiSchedule> findByLoanIdAndPaymentStatus(Long loanId, PaymentStatus paymentStatus);

    Optional<EmiSchedule> findByLoanIdAndEmiNumber(Long loanId, Integer emiNumber);

    List<EmiSchedule> findByPaymentStatus(PaymentStatus paymentStatus);

    List<EmiSchedule> findByDueDateBefore(LocalDate date);

    List<EmiSchedule> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    List<EmiSchedule> findByPaymentStatusAndDueDateBefore(PaymentStatus paymentStatus, LocalDate date);
}
