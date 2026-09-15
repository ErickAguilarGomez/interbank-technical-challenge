package com.interbank.batch.writer;

import com.interbank.batch.producer.TransactionKafkaProducer;
import com.interbank.common.dto.TransactionEvent;
import com.interbank.common.entity.TransactionRecord;
import com.interbank.common.repository.TransactionRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionBatchWriter implements ItemWriter<TransactionRecord> {

    private static final Logger log = LoggerFactory.getLogger(TransactionBatchWriter.class);

    private final TransactionRecordRepository transactionRecordRepository;
    private final TransactionKafkaProducer transactionKafkaProducer;

    public TransactionBatchWriter(
            TransactionRecordRepository transactionRecordRepository,
            TransactionKafkaProducer transactionKafkaProducer) {
        this.transactionRecordRepository = transactionRecordRepository;
        this.transactionKafkaProducer = transactionKafkaProducer;
    }

    @Override
    @Transactional
    public void write(Chunk<? extends TransactionRecord> chunk) {
        List<TransactionRecord> recordsToSave = new ArrayList<>(chunk.getItems());
        if (recordsToSave.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();
        List<TransactionRecord> savedRecords = transactionRecordRepository.saveAll(recordsToSave);
        transactionRecordRepository.flush();

        for (TransactionRecord record : savedRecords) {
            TransactionEvent event = TransactionEvent.builder()
                    .transactionId(record.getId())
                    .email(record.getEmail())
                    .country(record.getCountry())
                    .amount(record.getRandomFloat())
                    .status(record.getStatus())
                    .timestamp(Instant.now())
                    .build();

            transactionKafkaProducer.sendTransactionEvent(event);
        }

        long duration = System.currentTimeMillis() - start;
        log.info("Batch of {} records persisted and published in {} ms", savedRecords.size(), duration);
    }
}
