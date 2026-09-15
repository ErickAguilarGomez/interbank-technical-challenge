package com.interbank.common.dto;

import com.interbank.common.entity.TransactionRecord;
import com.interbank.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String city;
    private String country;
    private String firstname2;
    private String lastname2;
    private String email;
    private Integer age;
    private Integer randomInt;
    private Double randomFloat;
    private Boolean boolFlag;
    private LocalDate operationDate;
    private String regexPattern;
    private String channelEnum;
    private String elements;
    private TransactionStatus status;
    private String errorMessage;
    private Long processingDurationMs;
    private Instant createdAt;
    private Instant updatedAt;

    public static TransactionResponseDto fromEntity(TransactionRecord entity) {
        if (entity == null) {
            return null;
        }
        return TransactionResponseDto.builder()
                .id(entity.getId())
                .firstname(entity.getFirstname())
                .lastname(entity.getLastname())
                .city(entity.getCity())
                .country(entity.getCountry())
                .firstname2(entity.getFirstname2())
                .lastname2(entity.getLastname2())
                .email(entity.getEmail())
                .age(entity.getAge())
                .randomInt(entity.getRandomInt())
                .randomFloat(entity.getRandomFloat())
                .boolFlag(entity.getBoolFlag())
                .operationDate(entity.getOperationDate())
                .regexPattern(entity.getRegexPattern())
                .channelEnum(entity.getChannelEnum())
                .elements(entity.getElements())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .processingDurationMs(entity.getProcessingDurationMs())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

