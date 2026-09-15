package com.interbank.common.dto;

import com.interbank.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusCountResponse {
    private Map<TransactionStatus, Long> countsByStatus;
    private long totalRecords;
}

