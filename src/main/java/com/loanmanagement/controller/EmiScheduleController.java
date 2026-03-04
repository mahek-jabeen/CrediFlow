package com.loanmanagement.controller;

import com.loanmanagement.dto.ApiResponse;
import com.loanmanagement.entity.EmiSchedule;
import com.loanmanagement.entity.EmiSchedule.PaymentStatus;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.service.EmiScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/emi-schedules")
@RequiredArgsConstructor
@Slf4j
public class EmiScheduleController {

    private final EmiScheduleService emiScheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmiSchedule>> createEmiSchedule(@RequestBody EmiSchedule emiSchedule) {
        log.info("Creating new EMI schedule for loan: {}", emiSchedule.getLoan().getId());
        EmiSchedule created = emiScheduleService.createEmiSchedule(emiSchedule);
        return new ResponseEntity<>(ApiResponse.success("EMI schedule created successfully", created), 
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmiSchedule>> getEmiScheduleById(@PathVariable Long id) {
        log.info("Fetching EMI schedule with id: {}", id);
        EmiSchedule emiSchedule = emiScheduleService.getEmiScheduleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmiSchedule", "id", id));
        return ResponseEntity.ok(ApiResponse.success(emiSchedule));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmiSchedule>>> getAllEmiSchedules() {
        log.info("Fetching all EMI schedules");
        List<EmiSchedule> schedules = emiScheduleService.getAllEmiSchedules();
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<ApiResponse<List<EmiSchedule>>> getEmiSchedulesByLoanId(@PathVariable Long loanId) {
        log.info("Fetching EMI schedules for loan: {}", loanId);
        List<EmiSchedule> schedules = emiScheduleService.getEmiSchedulesByLoanId(loanId);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<EmiSchedule>>> getEmiSchedulesByStatus(
            @PathVariable PaymentStatus status) {
        log.info("Fetching EMI schedules with status: {}", status);
        List<EmiSchedule> schedules = emiScheduleService.getEmiSchedulesByPaymentStatus(status);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<EmiSchedule>>> getOverdueEmiSchedules() {
        log.info("Fetching overdue EMI schedules");
        List<EmiSchedule> schedules = emiScheduleService.getOverdueEmiSchedules();
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @GetMapping("/due-between")
    public ResponseEntity<ApiResponse<List<EmiSchedule>>> getEmiSchedulesDueBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching EMI schedules due between {} and {}", startDate, endDate);
        List<EmiSchedule> schedules = emiScheduleService.getEmiSchedulesDueBetween(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmiSchedule>> updateEmiSchedule(
            @PathVariable Long id, @RequestBody EmiSchedule emiSchedule) {
        log.info("Updating EMI schedule with id: {}", id);
        emiScheduleService.getEmiScheduleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmiSchedule", "id", id));
        EmiSchedule updated = emiScheduleService.updateEmiSchedule(id, emiSchedule);
        return ResponseEntity.ok(ApiResponse.success("EMI schedule updated successfully", updated));
    }

    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<ApiResponse<EmiSchedule>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status) {
        log.info("Updating payment status for EMI schedule id: {} to {}", id, status);
        EmiSchedule updated = emiScheduleService.updatePaymentStatus(id, status);
        if (updated == null) {
            throw new ResourceNotFoundException("EmiSchedule", "id", id);
        }
        return ResponseEntity.ok(ApiResponse.success("Payment status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmiSchedule(@PathVariable Long id) {
        log.info("Deleting EMI schedule with id: {}", id);
        emiScheduleService.getEmiScheduleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmiSchedule", "id", id));
        emiScheduleService.deleteEmiSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("EMI schedule deleted successfully", null));
    }
}
