package com.interbank.worker.service;

import com.interbank.common.dto.TransactionEvent;
import com.interbank.common.entity.TransactionRecord;
import com.interbank.common.enums.TransactionStatus;
import com.interbank.common.repository.TransactionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionProcessingServiceTest {

    @Mock
    private TransactionRecordRepository repository;

    private TransactionProcessingService service;

    @BeforeEach
    void setUp() {
        service = new TransactionProcessingService(repository, 0L);
    }

    @Test
    @DisplayName("Debe procesar exitosamente la transacción y actualizar estado a COMPLETED")
    void testProcessTransactionSuccess() {
        Long txId = 200L;
        TransactionRecord existingRecord = TransactionRecord.builder()
                .id(txId)
                .email("valid.user@yopmail.com")
                .country("Peru")
                .randomFloat(250.0)
                .status(TransactionStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(repository.findById(txId)).thenReturn(Optional.of(existingRecord));
        when(repository.save(any(TransactionRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionEvent event = TransactionEvent.builder()
                .transactionId(txId)
                .email("valid.user@yopmail.com")
                .country("Peru")
                .amount(250.0)
                .status(TransactionStatus.PENDING)
                .timestamp(Instant.now())
                .build();

        TransactionRecord result = service.processTransaction(event);

        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertNull(result.getErrorMessage());
        assertNotNull(result.getProcessingDurationMs());
        assertNotNull(result.getUpdatedAt());

        ArgumentCaptor<TransactionRecord> recordCaptor = ArgumentCaptor.forClass(TransactionRecord.class);
        verify(repository).save(recordCaptor.capture());
        assertEquals(TransactionStatus.COMPLETED, recordCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Debe marcar la transacción como FAILED si el email contiene 'invalid'")
    void testProcessTransactionFailureOnBusinessRule() {
        Long txId = 201L;
        TransactionRecord existingRecord = TransactionRecord.builder()
                .id(txId)
                .email("invalid.user@fraud.com")
                .status(TransactionStatus.PENDING)
                .build();

        when(repository.findById(txId)).thenReturn(Optional.of(existingRecord));
        when(repository.save(any(TransactionRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionEvent event = TransactionEvent.builder()
                .transactionId(txId)
                .email("invalid.user@fraud.com")
                .amount(100.0)
                .build();

        TransactionRecord result = service.processTransaction(event);

        assertNotNull(result);
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("Invalid email format"));
        verify(repository).save(existingRecord);
    }

    @Test
    @DisplayName("Debe manejar de forma segura si la transacción no existe en base de datos")
    void testProcessTransactionNotFound() {
        Long txId = 999L;
        when(repository.findById(txId)).thenReturn(Optional.empty());

        TransactionEvent event = TransactionEvent.builder()
                .transactionId(txId)
                .build();

        TransactionRecord result = service.processTransaction(event);

        assertNull(result);
        verify(repository, never()).save(any());
    }
}

