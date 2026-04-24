package com.schoolmanagement.dto.request;

public record ApprovePaymentRequest(
        boolean approved,
        String rejectionReason
) {}
