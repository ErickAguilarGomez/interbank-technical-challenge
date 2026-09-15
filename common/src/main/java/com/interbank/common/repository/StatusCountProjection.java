package com.interbank.common.repository;

import com.interbank.common.enums.TransactionStatus;

public interface StatusCountProjection {
    TransactionStatus getStatus();
    Long getCount();
}

