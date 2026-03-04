package com.loanmanagement.service;

import com.loanmanagement.entity.EmiSchedule;
import com.loanmanagement.entity.EmiSchedule.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmiScheduleService {

    EmiSchedule createEmiSchedule(EmiSchedule emiSchedule);

    EmiSchedule updateEmiSchedule(Long id, EmiSchedule emiSchedule);

    EmiSchedule updatePaymentStatus(Long id, PaymentStatus status);

    Optional<EmiSchedule> getEmiScheduleById(Long id);

    List<EmiSchedule> getAllEmiSchedules();

    List<EmiSchedule> getEmiSchedulesByLoanId(Long loanId);

    List<EmiSchedule> getEmiSchedulesByPaymentStatus(PaymentStatus status);

    List<EmiSchedule> getOverdueEmiSchedules();

    List<EmiSchedule> getEmiSchedulesDueBetween(LocalDate startDate, LocalDate endDate);

    void deleteEmiSchedule(Long id);
}
