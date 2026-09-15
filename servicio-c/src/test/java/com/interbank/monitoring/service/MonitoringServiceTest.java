package com.interbank.monitoring.service;

import com.interbank.common.dto.MetricsResponse;
import com.interbank.common.dto.StatusCountResponse;
import com.interbank.common.dto.TransactionResponseDto;
import com.interbank.common.entity.TransactionRecord;
import com.interbank.common.enums.TransactionStatus;
import com.interbank.common.repository.ProcessingMetricsProjection;
import com.interbank.common.repository.StatusCountProjection;
import com.interbank.common.repository.TransactionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock
    private TransactionRecordRepository repository;

    private MonitoringService service;

    @BeforeEach
    void setUp() {
        service = new MonitoringService(repository);
    }

    @Test
    @DisplayName("Debe retornar conteo agrupado por estado correctamente")
    void testGetStatusSummary() {
        StatusCountProjection proj1 = new StatusCountProjection() {
            @Override
            public TransactionStatus getStatus() {
                return TransactionStatus.COMPLETED;
            }

            @Override
            public Long getCount() {
                return 150L;
            }
        };

        StatusCountProjection proj2 = new StatusCountProjection() {
            @Override
            public TransactionStatus getStatus() {
                return TransactionStatus.FAILED;
            }

            @Override
            public Long getCount() {
                return 10L;
            }
        };

        when(repository.countGroupByStatus()).thenReturn(List.of(proj1, proj2));

        StatusCountResponse response = service.getStatusSummary();

        assertNotNull(response);
        assertEquals(160L, response.getTotalRecords());
        assertEquals(150L, response.getCountsByStatus().get(TransactionStatus.COMPLETED));
        assertEquals(10L, response.getCountsByStatus().get(TransactionStatus.FAILED));
        assertEquals(0L, response.getCountsByStatus().get(TransactionStatus.PENDING));
        assertEquals(0L, response.getCountsByStatus().get(TransactionStatus.PROCESSING));
    }

    @Test
    @DisplayName("Debe calcular métricas de procesamiento con proyección existente")
    void testGetMetricsWithData() {
        StatusCountProjection projCompleted = new StatusCountProjection() {
            @Override
            public TransactionStatus getStatus() {
                return TransactionStatus.COMPLETED;
            }

            @Override
            public Long getCount() {
                return 300L;
            }
        };

        when(repository.countGroupByStatus()).thenReturn(List.of(projCompleted));

        ProcessingMetricsProjection metricsProj = new ProcessingMetricsProjection() {
            @Override
            public Double getAvgDuration() {
                return 12.5;
            }

            @Override
            public Long getMinDuration() {
                return 10L;
            }

            @Override
            public Long getMaxDuration() {
                return 50L;
            }

            @Override
            public Long getCount() {
                return 300L;
            }
        };

        when(repository.getProcessingMetrics()).thenReturn(metricsProj);

        MetricsResponse response = service.getMetrics();

        assertNotNull(response);
        assertEquals(300L, response.getTotalProcessedRecords());
        assertEquals(300L, response.getCompletedRecords());
        assertEquals(0L, response.getFailedRecords());
        assertEquals(12.5, response.getAvgProcessingDurationMs());
        assertEquals(10L, response.getMinProcessingDurationMs());
        assertEquals(50L, response.getMaxProcessingDurationMs());
        assertNotNull(response.getProcessingThroughputPerSecond());
        assertEquals(240.0, response.getProcessingThroughputPerSecond());
    }

    @Test
    @DisplayName("Debe retornar métricas nulas de duración si no hay registros procesados")
    void testGetMetricsEmpty() {
        when(repository.countGroupByStatus()).thenReturn(List.of());
        when(repository.getProcessingMetrics()).thenReturn(null);

        MetricsResponse response = service.getMetrics();

        assertNotNull(response);
        assertEquals(0L, response.getTotalProcessedRecords());
        assertNull(response.getAvgProcessingDurationMs());
        assertNull(response.getMinProcessingDurationMs());
        assertNull(response.getMaxProcessingDurationMs());
        assertNull(response.getProcessingThroughputPerSecond());
    }

    @Test
    @DisplayName("Debe retornar página de registros filtrados por estado")
    void testGetPagedRecordsWithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        TransactionRecord record = TransactionRecord.builder()
                .id(1L)
                .firstname("Alex")
                .lastname("Smith")
                .email("alex@bank.com")
                .status(TransactionStatus.COMPLETED)
                .build();

        when(repository.findByStatus(TransactionStatus.COMPLETED, pageable))
                .thenReturn(new PageImpl<>(List.of(record), pageable, 1));

        Page<TransactionResponseDto> result = service.getPagedRecords(TransactionStatus.COMPLETED, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Alex", result.getContent().get(0).getFirstname());
        verify(repository).findByStatus(TransactionStatus.COMPLETED, pageable);
    }

    @Test
    @DisplayName("Debe retornar página de todos los registros cuando estado es null")
    void testGetPagedRecordsWithoutStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        TransactionRecord record = TransactionRecord.builder()
                .id(2L)
                .firstname("Maria")
                .lastname("Garcia")
                .status(TransactionStatus.PENDING)
                .build();

        when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(record), pageable, 1));

        Page<TransactionResponseDto> result = service.getPagedRecords(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Maria", result.getContent().get(0).getFirstname());
        verify(repository).findAll(pageable);
    }
}
