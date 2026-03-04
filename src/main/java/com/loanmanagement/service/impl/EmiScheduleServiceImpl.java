package com.loanmanagement.service.impl;

import com.loanmanagement.entity.EmiSchedule;
import com.loanmanagement.entity.EmiSchedule.PaymentStatus;
import com.loanmanagement.repository.EmiScheduleRepository;
import com.loanmanagement.service.EmiScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmiScheduleServiceImpl implements EmiScheduleService {

    private final EmiScheduleRepository emiScheduleRepository;

    @Override
    public EmiSchedule createEmiSchedule(EmiSchedule emiSchedule) {
        log.debug("Creating EMI schedule for loan: {}", emiSchedule.getLoan().getId());
        return emiScheduleRepository.save(emiSchedule);
    }

    @Override
    public EmiSchedule updateEmiSchedule(Long id, EmiSchedule emiSchedule) {
        log.debug("Updating EMI schedule with id: {}", id);
        emiSchedule.setId(id);
        return emiScheduleRepository.save(emiSchedule);
    }

    @Override
    public EmiSchedule updatePaymentStatus(Long id, PaymentStatus status) {
        log.debug("Updating EMI payment status for id: {} to {}", id, status);
        Optional<EmiSchedule> optionalEmi = emiScheduleRepository.findById(id);
        if (optionalEmi.isPresent()) {
            EmiSchedule emiSchedule = optionalEmi.get();
            emiSchedule.setPaymentStatus(status);
            if (status == PaymentStatus.PAID) {
                emiSchedule.setPaymentDate(LocalDate.now());
            }
            return emiScheduleRepository.save(emiSchedule);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmiSchedule> getEmiScheduleById(Long id) {
        log.debug("Fetching EMI schedule with id: {}", id);
        return emiScheduleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmiSchedule> getAllEmiSchedules() {
        log.debug("Fetching all EMI schedules");
        return emiScheduleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmiSchedule> getEmiSchedulesByLoanId(Long loanId) {
        log.debug("Fetching EMI schedules for loan: {}", loanId);
        return emiScheduleRepository.findByLoanId(loanId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmiSchedule> getEmiSchedulesByPaymentStatus(PaymentStatus status) {
        log.debug("Fetching EMI schedules with status: {}", status);
        return emiScheduleRepository.findByPaymentStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmiSchedule> getOverdueEmiSchedules() {
        log.debug("Fetching overdue EMI schedules");
        return emiScheduleRepository.findByPaymentStatusAndDueDateBefore(
                PaymentStatus.PENDING, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmiSchedule> getEmiSchedulesDueBetween(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching EMI schedules due between {} and {}", startDate, endDate);
        return emiScheduleRepository.findByDueDateBetween(startDate, endDate);
    }

    @Override
    public void deleteEmiSchedule(Long id) {
        log.debug("Deleting EMI schedule with id: {}", id);
        emiScheduleRepository.deleteById(id);
    }
}
