package com.interbank.worker.consumer;

import com.interbank.common.dto.TransactionEvent;
import com.interbank.worker.service.TransactionProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class TransactionKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionKafkaConsumer.class);

    private final TransactionProcessingService processingService;

    public TransactionKafkaConsumer(TransactionProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic:transactions-topic}",
            groupId = "${spring.kafka.consumer.group-id:transaction-worker-group}",
            concurrency = "${spring.kafka.listener.concurrency:3}"
    )
    public void consume(ConsumerRecord<String, TransactionEvent> record, Acknowledgment ack) {
        log.debug("Received kafka record topic={} partition={} offset={} key={}",
                record.topic(), record.partition(), record.offset(), record.key());

        try {
            TransactionEvent event = record.value();
            if (event != null) {
                processingService.processTransaction(event);
            }
            if (ack != null) {
                ack.acknowledge();
            }
        } catch (Exception e) {
            log.error("Failed to process kafka record key={}: {}", record.key(), e.getMessage(), e);
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }
}

