package com.interbank.monitoring.service;

import com.interbank.common.dto.MetricsResponse;
import com.interbank.common.dto.StatusCountResponse;
import com.interbank.common.dto.TransactionResponseDto;
import com.interbank.common.entity.TransactionRecord;
import com.interbank.common.enums.TransactionStatus;
import com.interbank.common.repository.ProcessingMetricsProjection;
import com.interbank.common.repository.StatusCountProjection;
import com.interbank.common.repository.TransactionRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MonitoringService {

    private final TransactionRecordRepository repository;

    public MonitoringService(TransactionRecordRepository repository) {
        this.repository = repository;
    }

    public StatusCountResponse getStatusSummary() {
        List<StatusCountProjection> projections = repository.countGroupByStatus();
        Map<TransactionStatus, Long> countsMap = new EnumMap<>(TransactionStatus.class);

        for (TransactionStatus status : TransactionStatus.values()) {
            countsMap.put(status, 0L);
        }

        long total = 0;
        for (StatusCountProjection p : projections) {
            countsMap.put(p.getStatus(), p.getCount());
            total += p.getCount();
        }

        return StatusCountResponse.builder()
                .countsByStatus(countsMap)
                .totalRecords(total)
                .build();
    }

    public Page<TransactionResponseDto> getPagedRecords(TransactionStatus status, Pageable pageable) {
        Page<TransactionRecord> page;
        if (status != null) {
            page = repository.findByStatus(status, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(TransactionResponseDto::fromEntity);
    }

    public MetricsResponse getMetrics() {
        StatusCountResponse summary = getStatusSummary();
        Map<TransactionStatus, Long> counts = summary.getCountsByStatus();

        long completed = counts.getOrDefault(TransactionStatus.COMPLETED, 0L);
        long failed = counts.getOrDefault(TransactionStatus.FAILED, 0L);
        long pending = counts.getOrDefault(TransactionStatus.PENDING, 0L);
        long processing = counts.getOrDefault(TransactionStatus.PROCESSING, 0L);
        long totalProcessed = completed + failed;

        ProcessingMetricsProjection proj = repository.getProcessingMetrics();

        Double avgDuration = null;
        Long minDuration = null;
        Long maxDuration = null;
        Double throughput = null;

        if (proj != null && proj.getCount() != null && proj.getCount() > 0) {
            avgDuration = proj.getAvgDuration();
            minDuration = proj.getMinDuration();
            maxDuration = proj.getMaxDuration();

            if (avgDuration != null && avgDuration > 0) {
                throughput = Math.round(((1000.0 / avgDuration) * 3.0) * 100.0) / 100.0;
            }
        }

        return MetricsResponse.builder()
                .totalProcessedRecords(totalProcessed)
                .completedRecords(completed)
                .failedRecords(failed)
                .pendingRecords(pending)
                .inProcessingRecords(processing)
                .avgProcessingDurationMs(avgDuration)
                .minProcessingDurationMs(minDuration)
                .maxProcessingDurationMs(maxDuration)
                .processingThroughputPerSecond(throughput)
                .build();
    }
}

