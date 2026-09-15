package com.interbank.common.repository;

import com.interbank.common.entity.TransactionRecord;
import com.interbank.common.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    Page<TransactionRecord> findByStatus(TransactionStatus status, Pageable pageable);

    @Query("SELECT t.status AS status, COUNT(t) AS count FROM TransactionRecord t GROUP BY t.status")
    List<StatusCountProjection> countGroupByStatus();

    @Query("SELECT AVG(t.processingDurationMs) AS avgDuration, " +
           "MIN(t.processingDurationMs) AS minDuration, " +
           "MAX(t.processingDurationMs) AS maxDuration, " +
           "COUNT(t) AS count " +
           "FROM TransactionRecord t WHERE t.processingDurationMs IS NOT NULL")
    ProcessingMetricsProjection getProcessingMetrics();
}

