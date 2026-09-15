package com.interbank.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto implements Serializable {
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
    private List<String> elements;
}

