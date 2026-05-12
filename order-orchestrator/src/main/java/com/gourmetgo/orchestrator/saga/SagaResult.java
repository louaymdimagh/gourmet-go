package com.gourmetgo.orchestrator.saga;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SagaResult {
    private boolean success;
    private String message;
    private String orderId;
    private String status;

    public static SagaResult success(String message, String orderId, String status) {
        return SagaResult.builder()
                .success(true)
                .message(message)
                .orderId(orderId)
                .status(status)
                .build();
    }

    public static SagaResult failure(String message) {
        return SagaResult.builder()
                .success(false)
                .message(message)
                .status("REJECTED")
                .build();
    }
}
