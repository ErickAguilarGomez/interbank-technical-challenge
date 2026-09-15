package com.interbank.monitoring.controller;

import com.interbank.common.dto.MetricsResponse;
import com.interbank.common.dto.StatusCountResponse;
import com.interbank.common.dto.TransactionResponseDto;
import com.interbank.common.enums.TransactionStatus;
import com.interbank.monitoring.service.MonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MonitoringController.class)
class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonitoringService monitoringService;

    @Test
    @DisplayName("GET /api/v1/monitoring/status debe retornar totales agrupados por estado")
    void testGetStatusSummary() throws Exception {
        StatusCountResponse response = StatusCountResponse.builder()
                .countsByStatus(Map.of(
                        TransactionStatus.PENDING, 50L,
                        TransactionStatus.PROCESSING, 10L,
                        TransactionStatus.COMPLETED, 4900L,
                        TransactionStatus.FAILED, 40L
                ))
                .totalRecords(5000L)
                .build();

        when(monitoringService.getStatusSummary()).thenReturn(response);

        mockMvc.perform(get("/api/v1/monitoring/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(5000))
                .andExpect(jsonPath("$.countsByStatus.COMPLETED").value(4900))
                .andExpect(jsonPath("$.countsByStatus.PENDING").value(50))
                .andExpect(jsonPath("$.countsByStatus.FAILED").value(40));
    }

    @Test
    @DisplayName("GET /api/v1/monitoring/records debe retornar lista paginada de transacciones")
    void testGetPagedRecords() throws Exception {
        TransactionResponseDto dto = TransactionResponseDto.builder()
                .id(1L)
                .firstname("Erick")
                .lastname("Developer")
                .email("erick@bank.com")
                .status(TransactionStatus.COMPLETED)
                .createdAt(Instant.now())
                .build();

        Page<TransactionResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        when(monitoringService.getPagedRecords(eq(TransactionStatus.COMPLETED), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/monitoring/records")
                        .param("status", "COMPLETED")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].firstname").value("Erick"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/monitoring/metrics debe retornar métricas de procesamiento y latencia")
    void testGetMetrics() throws Exception {
        MetricsResponse response = MetricsResponse.builder()
                .totalProcessedRecords(4940L)
                .completedRecords(4900L)
                .failedRecords(40L)
                .pendingRecords(60L)
                .avgProcessingDurationMs(12.5)
                .minProcessingDurationMs(2L)
                .maxProcessingDurationMs(45L)
                .processingThroughputPerSecond(240.0)
                .build();

        when(monitoringService.getMetrics()).thenReturn(response);

        mockMvc.perform(get("/api/v1/monitoring/metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessedRecords").value(4940))
                .andExpect(jsonPath("$.avgProcessingDurationMs").value(12.5))
                .andExpect(jsonPath("$.minProcessingDurationMs").value(2))
                .andExpect(jsonPath("$.maxProcessingDurationMs").value(45))
                .andExpect(jsonPath("$.processingThroughputPerSecond").value(240.0));
    }
}
