package com.interbank.worker.consumer;

import com.interbank.common.dto.TransactionEvent;
import com.interbank.worker.service.TransactionProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionKafkaConsumerTest {

    @Mock
    private TransactionProcessingService processingService;

    @Mock
    private Acknowledgment acknowledgment;

    private TransactionKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new TransactionKafkaConsumer(processingService);
    }

    @Test
    @DisplayName("Debe procesar el mensaje y confirmar el offset mediante Acknowledgment")
    void testConsumeAndAcknowledge() {
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(500L)
                .email("test@yopmail.com")
                .build();

        ConsumerRecord<String, TransactionEvent> record = new ConsumerRecord<>(
                "transactions-topic", 0, 10L, "500", event
        );

        consumer.consume(record, acknowledgment);

        verify(processingService).processTransaction(event);
        verify(acknowledgment).acknowledge();
    }
}

