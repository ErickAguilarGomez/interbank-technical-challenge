package com.interbank.batch.producer;

import com.interbank.common.dto.TransactionEvent;
import com.interbank.common.enums.TransactionStatus;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    private TransactionKafkaProducer producer;
    private final String testTopic = "transactions-topic";

    @BeforeEach
    void setUp() {
        producer = new TransactionKafkaProducer(kafkaTemplate, testTopic);
    }

    @Test
    @DisplayName("Debe publicar evento en Kafka exitosamente con clave y payload correctos")
    void testSendTransactionEventSuccess() throws Exception {
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(101L)
                .email("test@yopmail.com")
                .country("Peru")
                .amount(1500.50)
                .status(TransactionStatus.PENDING)
                .timestamp(Instant.now())
                .build();

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(testTopic, 0),
                0L, 0, 0L, 0, 0
        );
        SendResult<String, TransactionEvent> sendResult = new SendResult<>(null, metadata);
        CompletableFuture<SendResult<String, TransactionEvent>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(eq(testTopic), eq("101"), any(TransactionEvent.class))).thenReturn(future);

        CompletableFuture<SendResult<String, TransactionEvent>> result = producer.sendTransactionEvent(event);

        assertNotNull(result);
        SendResult<String, TransactionEvent> actualResult = result.get();
        assertEquals(testTopic, actualResult.getRecordMetadata().topic());

        ArgumentCaptor<TransactionEvent> eventCaptor = ArgumentCaptor.forClass(TransactionEvent.class);
        verify(kafkaTemplate).send(eq(testTopic), eq("101"), eventCaptor.capture());
        assertEquals(101L, eventCaptor.getValue().getTransactionId());
        assertEquals("test@yopmail.com", eventCaptor.getValue().getEmail());
    }

    @Test
    @DisplayName("Debe manejar fallos asíncronos en el envío a Kafka")
    void testSendTransactionEventFailure() {
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(999L)
                .email("fail@yopmail.com")
                .build();

        CompletableFuture<SendResult<String, TransactionEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka broker connection timeout"));

        when(kafkaTemplate.send(eq(testTopic), eq("999"), any(TransactionEvent.class))).thenReturn(failedFuture);

        CompletableFuture<SendResult<String, TransactionEvent>> result = producer.sendTransactionEvent(event);
        assertNotNull(result);
        assertEquals(true, result.isCompletedExceptionally());
    }
}

