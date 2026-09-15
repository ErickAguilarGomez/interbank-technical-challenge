package com.interbank.common.dto;

import com.interbank.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent implements Serializable {
    private Long transactionId;
    private String email;
    private String country;
    private Double amount;
    private TransactionStatus status;
    private Instant timestamp;
}

