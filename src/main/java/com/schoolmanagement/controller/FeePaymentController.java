package com.schoolmanagement.controller;

import com.schoolmanagement.dto.request.ApprovePaymentRequest;
import com.schoolmanagement.dto.request.RecordPaymentRequest;
import com.schoolmanagement.dto.response.FeePaymentResponse;
import com.schoolmanagement.dto.response.PageResponse;
import com.schoolmanagement.service.FeePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fee-payments")
@RequiredArgsConstructor
public class FeePaymentController {

    private final FeePaymentService feePaymentService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<FeePaymentResponse> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return feePaymentService.getPendingPayments(page, size);
    }

    @GetMapping("/pending/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countPending() {
        return ResponseEntity.ok(Map.of("count", feePaymentService.countPendingApprovals()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_EMPLOYEE')")
    public ResponseEntity<FeePaymentResponse> record(@Valid @RequestBody RecordPaymentRequest req) {
        return ResponseEntity.status(201).body(feePaymentService.recordPayment(req));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public FeePaymentResponse approveOrReject(@PathVariable Long id,
                                               @RequestBody ApprovePaymentRequest req) {
        return feePaymentService.approveOrReject(id, req);
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id) {
        byte[] pdf = feePaymentService.generateReceiptPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"receipt-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
