package com.interbank.batch.processor;

import com.interbank.common.dto.TransactionDto;
import com.interbank.common.entity.TransactionRecord;
import com.interbank.common.enums.TransactionStatus;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TransactionItemProcessor implements ItemProcessor<TransactionDto, TransactionRecord> {

    @Override
    public TransactionRecord process(TransactionDto dto) {
        if (dto == null) {
            return null;
        }

        String elementsJoined = null;
        if (dto.getElements() != null && !dto.getElements().isEmpty()) {
            elementsJoined = String.join(",", dto.getElements());
        }

        return TransactionRecord.builder()
                .firstname(dto.getFirstname())
                .lastname(dto.getLastname())
                .city(dto.getCity())
                .country(dto.getCountry())
                .firstname2(dto.getFirstname2())
                .lastname2(dto.getLastname2())
                .email(dto.getEmail())
                .age(dto.getAge())
                .randomInt(dto.getRandomInt())
                .randomFloat(dto.getRandomFloat())
                .boolFlag(dto.getBoolFlag())
                .operationDate(dto.getOperationDate())
                .regexPattern(dto.getRegexPattern())
                .channelEnum(dto.getChannelEnum())
                .elements(elementsJoined)
                .status(TransactionStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }
}

