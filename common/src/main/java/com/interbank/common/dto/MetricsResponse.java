package com.interbank.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponse {
    private long totalProcessedRecords;
    private long completedRecords;
    private long failedRecords;
    private long pendingRecords;
    private long inProcessingRecords;
    private Double avgProcessingDurationMs;
    private Long minProcessingDurationMs;
    private Long maxProcessingDurationMs;
    private Double processingThroughputPerSecond;
}

