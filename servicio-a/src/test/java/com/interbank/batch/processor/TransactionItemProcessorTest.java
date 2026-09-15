package com.interbank.batch.processor;

import com.interbank.common.dto.TransactionDto;
import com.interbank.common.entity.TransactionRecord;
import com.interbank.common.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionItemProcessorTest {

    private TransactionItemProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TransactionItemProcessor();
    }

    @Test
    @DisplayName("Debe transformar un TransactionDto a TransactionRecord con estado PENDING")
    void testProcessValidDto() {
        TransactionDto dto = TransactionDto.builder()
                .firstname("John")
                .lastname("Doe")
                .city("Lima")
                .country("Peru")
                .firstname2("Jane")
                .lastname2("Smith")
                .email("john.doe@test.com")
                .age(30)
                .randomInt(10)
                .randomFloat(25.5)
                .boolFlag(true)
                .operationDate(LocalDate.of(2024, 1, 15))
                .regexPattern("sample-pattern")
                .channelEnum("web")
                .elements(List.of("alpha", "beta", "gamma"))
                .build();

        TransactionRecord record = processor.process(dto);

        assertNotNull(record);
        assertEquals("John", record.getFirstname());
        assertEquals("Doe", record.getLastname());
        assertEquals("Lima", record.getCity());
        assertEquals("Peru", record.getCountry());
        assertEquals("Jane", record.getFirstname2());
        assertEquals("Smith", record.getLastname2());
        assertEquals("john.doe@test.com", record.getEmail());
        assertEquals(30, record.getAge());
        assertEquals(10, record.getRandomInt());
        assertEquals(25.5, record.getRandomFloat());
        assertTrue(record.getBoolFlag());
        assertEquals(LocalDate.of(2024, 1, 15), record.getOperationDate());
        assertEquals("sample-pattern", record.getRegexPattern());
        assertEquals("web", record.getChannelEnum());
        assertEquals("alpha,beta,gamma", record.getElements());
        assertEquals(TransactionStatus.PENDING, record.getStatus());
        assertNotNull(record.getCreatedAt());
    }

    @Test
    @DisplayName("Debe manejar DTO nulo retornando null")
    void testProcessNullDto() {
        TransactionRecord record = processor.process(null);
        assertNull(record);
    }
}

