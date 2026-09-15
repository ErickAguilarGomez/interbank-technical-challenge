package com.interbank.batch.producer;

import com.interbank.common.dto.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransactionKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(TransactionKafkaProducer.class);

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final String topic;

    public TransactionKafkaProducer(
            KafkaTemplate<String, TransactionEvent> kafkaTemplate,
            @Value("${app.kafka.topic:transactions-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public CompletableFuture<SendResult<String, TransactionEvent>> sendTransactionEvent(TransactionEvent event) {
        String key = event.getTransactionId() != null ? String.valueOf(event.getTransactionId()) : null;

        CompletableFuture<SendResult<String, TransactionEvent>> future = kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send transaction event txId={}: {}", event.getTransactionId(), ex.getMessage());
            } else {
                log.debug("Published event txId={} topic={} partition={} offset={}",
                        event.getTransactionId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
        return future;
    }
}
