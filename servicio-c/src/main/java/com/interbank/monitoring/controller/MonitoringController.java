package com.interbank.monitoring.controller;

import com.interbank.common.dto.MetricsResponse;
import com.interbank.common.dto.StatusCountResponse;
import com.interbank.common.dto.TransactionResponseDto;
import com.interbank.common.enums.TransactionStatus;
import com.interbank.monitoring.service.MonitoringService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/status")
    public ResponseEntity<StatusCountResponse> getStatusSummary() {
        StatusCountResponse response = monitoringService.getStatusSummary();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/records")
    public ResponseEntity<Page<TransactionResponseDto>> getPagedRecords(
            @RequestParam(required = false) TransactionStatus status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<TransactionResponseDto> page = monitoringService.getPagedRecords(status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        MetricsResponse response = monitoringService.getMetrics();
        return ResponseEntity.ok(response);
    }
}

