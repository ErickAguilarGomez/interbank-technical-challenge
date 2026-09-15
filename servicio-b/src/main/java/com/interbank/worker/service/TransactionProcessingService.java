package com.interbank.worker.service;

import com.interbank.common.dto.TransactionEvent;
import com.interbank.common.entity.TransactionRecord;
import com.interbank.common.enums.TransactionStatus;
import com.interbank.common.repository.TransactionRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class TransactionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessingService.class);

    private final TransactionRecordRepository repository;
    private final long simulatedDelayMs;

    public TransactionProcessingService(
            TransactionRecordRepository repository,
            @Value("${app.worker.simulated-delay-ms:10}") long simulatedDelayMs) {
        this.repository = repository;
        this.simulatedDelayMs = simulatedDelayMs;
    }

    @Transactional
    public TransactionRecord processTransaction(TransactionEvent event) {
        if (event == null || event.getTransactionId() == null) {
            log.warn("Null or invalid transaction event received");
            return null;
        }

        long startTime = System.currentTimeMillis();
        Long txId = event.getTransactionId();
        log.debug("Processing transaction txId={}", txId);

        Optional<TransactionRecord> optionalRecord = repository.findById(txId);
        if (optionalRecord.isEmpty()) {
            log.error("Transaction not found txId={}", txId);
            return null;
        }

        TransactionRecord record = optionalRecord.get();

        try {
            if (simulatedDelayMs > 0) {
                Thread.sleep(simulatedDelayMs);
            }

            if (event.getEmail() != null && event.getEmail().contains("invalid")) {
                throw new IllegalArgumentException("Invalid email format detected");
            }

            if (event.getAmount() != null && event.getAmount() < 0) {
                throw new IllegalArgumentException("Negative amount is not allowed");
            }

            record.setStatus(TransactionStatus.COMPLETED);
            record.setErrorMessage(null);
            log.debug("Transaction completed txId={}", txId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            record.setStatus(TransactionStatus.FAILED);
            record.setErrorMessage("Processing interrupted: " + e.getMessage());
            log.error("Transaction processing interrupted txId={}", txId);
        } catch (Exception e) {
            record.setStatus(TransactionStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            log.warn("Transaction processing failed txId={}: {}", txId, e.getMessage());
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            record.setProcessingDurationMs(duration);
            record.setUpdatedAt(Instant.now());
        }

        return repository.save(record);
    }
}

